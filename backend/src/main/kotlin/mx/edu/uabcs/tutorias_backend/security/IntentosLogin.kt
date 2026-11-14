package mx.edu.uabcs.tutorias_backend.security

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class IntentosLogin {

    var ahora: () -> Instant = Instant::now 

    private val fallos = ConcurrentHashMap<String, List<Instant>>()

    fun bloqueado(correo: String): Boolean = recientes(fallos[correo].orEmpty()).size >= MAX_FALLOS

    fun fallo(correo: String) {
        fallos.compute(correo) { _, previos -> recientes(previos.orEmpty()) + ahora() }
    }

    fun exito(correo: String) {
        fallos.remove(correo)
    }

    private fun recientes(lista: List<Instant>): List<Instant> {
        val desde = ahora().minus(VENTANA)
        return lista.filter { it.isAfter(desde) }
    }

    companion object {
        const val MAX_FALLOS = 5
        val VENTANA: Duration = Duration.ofMinutes(15)
    }
}