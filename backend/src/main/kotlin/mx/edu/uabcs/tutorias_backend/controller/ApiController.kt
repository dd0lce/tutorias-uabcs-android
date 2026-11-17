package mx.edu.uabcs.tutorias_backend.controller

import java.time.LocalDate
import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad
import mx.edu.uabcs.tutorias_backend.service.ServicioTutorias
import mx.edu.uabcs.tutorias_backend.service.SolicitudDto
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Cuerpo de las acciones. Cada endpoint usa solo los campos que necesita. */
data class Cuerpo(
    val inicio: LocalDateTime? = null,
    val mensaje: String? = null,
    val motivo: String? = null,
    val asistio: Boolean? = null,
    val notas: String? = null,
    val permitir: Boolean? = null,
    val lunes: LocalDate? = null,
    val abiertos: Set<LocalDateTime>? = null,
    val materia: String? = null,
    val modalidades: Set<Modalidad>? = null,
    val formatos: Set<Formato>? = null,
    val confirmaAutomatico: Boolean? = null,
    val ids: List<String>? = null,
    val texto: String? = null,
)

private fun <T> T?.dato(nombre: String): T = requireNotNull(this) { "Falta el campo $nombre." }

private val Jwt.yo: Long get() = checkNotNull(subject).toLong()

@RestController
@RequestMapping("/api")
class ApiController(private val tutorias: ServicioTutorias) {

    @GetMapping("/datos")
    fun datos(@AuthenticationPrincipal jwt: Jwt) = tutorias.datos(jwt.yo)

    @PostMapping("/solicitudes")
    fun enviar(@AuthenticationPrincipal jwt: Jwt, @RequestBody s: SolicitudDto) = tutorias.enviar(jwt.yo, s)

    @PostMapping("/solicitudes/{id}/{accion}")
    fun accion(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String,
        @PathVariable accion: String,
        @RequestBody(required = false) c: Cuerpo?,
    ): SolicitudDto {
        val b = c ?: Cuerpo()
        return when (accion) {
            "cancelar" -> tutorias.cancelar(jwt.yo, id)
            "cambiar-horario" -> tutorias.cambiarHorario(jwt.yo, id, b.inicio.dato("inicio"))
            "solicitar-cambio" -> tutorias.solicitarCambio(jwt.yo, id, b.mensaje.dato("mensaje"))
            "coordinacion" -> tutorias.permitirCoordinacion(jwt.yo, id, b.permitir.dato("permitir"))
            "aceptar" -> tutorias.aceptar(jwt.yo, id)
            "rechazar" -> tutorias.rechazar(jwt.yo, id, b.motivo.dato("motivo"))
            "cancelar-tutor" -> tutorias.cancelarComoTutor(jwt.yo, id, b.motivo.dato("motivo"))
            "registrar" -> tutorias.registrarSesion(jwt.yo, id, b.asistio.dato("asistio"), b.notas.orEmpty())
            else -> throw IllegalArgumentException("Acción desconocida: $accion")
        }
    }

    @GetMapping("/solicitudes/{id}/mensajes")
    fun mensajes(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String, @RequestParam(defaultValue = "0") despuesDe: Long) =
        tutorias.mensajes(jwt.yo, id, despuesDe)

    @PostMapping("/solicitudes/{id}/mensajes")
    fun enviarMensaje(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String, @RequestBody c: Cuerpo) =
        tutorias.enviarMensaje(jwt.yo, id, c.texto.dato("texto"))

    @PostMapping("/tutores/{id}/{accion}")
    fun tutor(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String, @PathVariable accion: String, @RequestBody c: Cuerpo) =
        when (accion) {
            "semana" -> tutorias.guardarSemana(jwt.yo, id, c.lunes.dato("lunes"), c.abiertos.dato("abiertos"))
            "copiar-semana" -> tutorias.copiarSemana(jwt.yo, id, c.lunes.dato("lunes"))
            "agregar-materia" -> tutorias.agregarMateria(jwt.yo, id, c.materia.dato("materia"))
            "quitar-materia" -> tutorias.quitarMateria(jwt.yo, id, c.materia.dato("materia"))
            "modalidades" -> tutorias.ajustarModalidades(
                jwt.yo, id, c.modalidades.dato("modalidades"), c.formatos.dato("formatos"), c.confirmaAutomatico.dato("confirmaAutomatico"),
            )
            else -> throw IllegalArgumentException("Acción desconocida: $accion")
        }

    @PostMapping("/avisos/leidos")
    fun leidos(@AuthenticationPrincipal jwt: Jwt, @RequestBody c: Cuerpo) = tutorias.marcarLeidos(jwt.yo, c.ids.dato("ids"))
}
