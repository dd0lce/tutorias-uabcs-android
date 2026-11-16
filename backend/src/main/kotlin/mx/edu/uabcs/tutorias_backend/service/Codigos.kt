package mx.edu.uabcs.tutorias_backend.service

import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import mx.edu.uabcs.tutorias_backend.entity.Codigo
import mx.edu.uabcs.tutorias_backend.repository.CodigoRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/** Códigos de 6 dígitos por correo para registrarse o recuperar la contraseña. */
@Service
@Transactional
class Codigos(
    private val codigos: CodigoRepository,
    private val encoder: PasswordEncoder,
    private val correo: Correo,
) {
    private val azar = SecureRandom()

    fun enviar(destino: String, proposito: String) {
        val previo = codigos.findById(destino).orElse(null)
        // Un código por minuto como máximo.
        if (previo != null && previo.proposito == proposito && previo.expira.isAfter(Instant.now().plus(VIGENCIA).minus(ESPERA))) return
        val codigo = "%06d".format(azar.nextInt(1_000_000))
        codigos.save(Codigo(destino, proposito, checkNotNull(encoder.encode(codigo)), Instant.now().plus(VIGENCIA)))
        correo.enviar(destino, "Tu código de Tutorías UABCS", "Tu código es $codigo. Vence en ${VIGENCIA.toMinutes()} minutos.")
    }

    /** Lanza [IllegalArgumentException] si el código no sirve. Un código correcto ya no se puede volver a usar. */
    // Transacción propia: el intento fallido se guarda aunque quien llama revierta la suya.
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = [IllegalArgumentException::class])
    fun verificar(destino: String, proposito: String, codigo: String) {
        val k = codigos.findById(destino).orElse(null)
        require(k != null && k.proposito == proposito && k.expira.isAfter(Instant.now()) && k.intentos < MAX_INTENTOS) {
            "El código venció o ya no es válido. Pide uno nuevo."
        }
        if (!encoder.matches(codigo.trim(), k.codigoHash)) {
            k.intentos++
            throw IllegalArgumentException("Código incorrecto.")
        }
        codigos.delete(k)
    }

    companion object {
        val VIGENCIA: Duration = Duration.ofMinutes(15)
        val ESPERA: Duration = Duration.ofMinutes(1)
        const val MAX_INTENTOS = 5
    }
}
