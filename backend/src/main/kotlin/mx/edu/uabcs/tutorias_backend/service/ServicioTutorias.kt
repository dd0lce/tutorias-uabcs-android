package mx.edu.uabcs.tutorias_backend.service

import java.time.DayOfWeek
import java.time.Instant
import java.util.Base64
import java.time.LocalDate
import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.config.Reloj
import mx.edu.uabcs.tutorias_backend.entity.AvisoInstitucional
import mx.edu.uabcs.tutorias_backend.entity.Mensaje
import mx.edu.uabcs.tutorias_backend.entity.Solicitud
import mx.edu.uabcs.tutorias_backend.entity.Tutor
import mx.edu.uabcs.tutorias_backend.entity.Usuario
import mx.edu.uabcs.tutorias_backend.model.Estado
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad
import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.model.TipoSesion
import mx.edu.uabcs.tutorias_backend.repository.AvisoInstitucionalRepository
import mx.edu.uabcs.tutorias_backend.repository.MensajeRepository
import mx.edu.uabcs.tutorias_backend.repository.SolicitudRepository
import mx.edu.uabcs.tutorias_backend.repository.TutorRepository
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

data class TutorDto(
    val id: String,
    val nombre: String,
    val titulo: String,
    val area: String,
    val resumen: String,
    val materias: List<String>,
    val calificacion: Double,
    val sesiones: Int,
    val modalidades: Set<Modalidad>,
    val formatos: Set<Formato>,
    val lugar: String,
    val confirmaAutomatico: Boolean,
    val experiencia: List<String>,
    val horarios: List<LocalDateTime>,
    /** Solo si el tutor eligió mostrarlo. */
    val correo: String? = null,
)

data class AvisoInstitucionalDto(val id: String, val titulo: String, val detalle: String, val hasta: LocalDate)

data class SolicitudDto(
    val id: String,
    val tutorId: String,
    val tipo: TipoSesion,
    val modalidad: Modalidad,
    val formato: Formato,
    val companeros: List<String> = emptyList(),
    val materia: String,
    val tema: String,
    val objetivo: String,
    val archivo: String? = null,
    val inicio: LocalDateTime? = null,
    val estado: Estado = Estado.PENDIENTE,
    val nota: String? = null,
    val coordinacionPuedeVer: Boolean = false,
    val asistio: Boolean? = null,
    val notasSesion: String? = null,
    /** Solo de salida: lo pone el servidor con la cuenta del estudiante. `null` para Coordinación sin permiso. */
    val estudiante: EstudianteDto? = null,
    /** Solo de salida, en hora de La Paz. */
    val creadaEn: LocalDateTime? = null,
    /** Solo de entrada: contenido de [archivo] en Base64. Se descarga con `GET /api/solicitudes/{id}/archivo`. */
    val archivoBase64: String? = null,
)

data class EstudianteDto(val correo: String, val nombre: String, val detalle: String)

data class DatosDto(
    val tutores: List<TutorDto>,
    val solicitudes: List<SolicitudDto>,
    val leidos: List<String>,
    val avisos: List<AvisoInstitucionalDto>,
)

data class MensajeDto(val id: Long, val autor: String, val propio: Boolean, val texto: String, val enviadoEn: Instant)

/**
 * Reglas de las tutorías. Son las mismas que `TutoriasRepo` de la app: la app las aplica al instante
 * y el servidor las vuelve a revisar, porque es quien decide.
 */
@Service
@Transactional
class ServicioTutorias(
    private val tutores: TutorRepository,
    private val solicitudes: SolicitudRepository,
    private val usuarios: UsuarioRepository,
    private val mensajes: MensajeRepository,
    private val reloj: Reloj,
    private val jdbc: JdbcTemplate,
    private val avisos: AvisoInstitucionalRepository,
) {
    @Transactional(readOnly = true)
    fun datos(yo: Long): DatosDto {
        val u = usuario(yo)
        val propio = if (u.rol == Rol.TUTOR) tutores.findByUsuarioId(yo) else null
        val lista = when (u.rol) {
            Rol.ESTUDIANTE -> solicitudes.findByEstudianteId(yo)
            Rol.TUTOR -> propio?.let { solicitudes.findByTutorId(it.id) }.orEmpty()
            Rol.COORDINACION -> solicitudes.findAll()
        }
        // Los horarios que ocupan otros estudiantes no se mandan: la app solo conoce las solicitudes propias.
        val ocupados = solicitudes.findByInicioNotNullAndEstadoIn(ACTIVAS)
            .filter { it.estudiante.id != yo }
            .groupBy({ it.tutor.id }, { it.inicio!! })
        return DatosDto(
            tutores.findAll().sortedBy { it.id }.map { t ->
                t.dto(if (t.id == propio?.id) emptySet() else ocupados[t.id].orEmpty().toSet())
            },
            lista.sortedBy { it.creadaEn }.map { it.dto(privada = u.rol == Rol.COORDINACION && !it.coordinacionPuedeVer) },
            u.leidos.toList(),
            avisos.findByHastaGreaterThanEqualOrderByPublicadoEnDesc(reloj.ahora().toLocalDate()).map { it.dto() },
        )
    }

    /** El id lo genera la app, como en las solicitudes. */
    fun publicarAviso(yo: Long, d: AvisoInstitucionalDto): AvisoInstitucionalDto {
        if (usuario(yo).rol != Rol.COORDINACION) prohibido()
        val hoy = reloj.ahora().toLocalDate()
        require(d.id.isNotBlank() && d.id.length <= 40 && !avisos.existsById(d.id)) { "Identificador de aviso inválido." }
        require(d.titulo.trim().length in 3..120) { "El título debe tener entre 3 y 120 caracteres." }
        require(d.detalle.trim().length in 3..500) { "El detalle debe tener entre 3 y 500 caracteres." }
        require(d.hasta in hoy..hoy.plusYears(1)) { "La fecha debe estar entre hoy y dentro de un año." }
        val a = AvisoInstitucional(d.id, d.titulo.trim(), d.detalle.trim(), d.hasta, reloj.ahora().atZone(Reloj.ZONA).toInstant())
        return avisos.save(a).dto()
    }

    fun quitarAviso(yo: Long, id: String) {
        if (usuario(yo).rol != Rol.COORDINACION) prohibido()
        avisos.deleteById(id)
    }

    fun enviar(yo: Long, d: SolicitudDto): SolicitudDto {
        val u = usuario(yo)
        if (u.rol != Rol.ESTUDIANTE) prohibido()
        require(d.id.isNotBlank() && d.id.length <= 40 && !solicitudes.existsById(d.id)) { "Identificador de solicitud inválido." }
        val t = tutor(d.tutorId)
        val inicio = d.inicio
        require(inicio == null || inicio in horariosLibres(t)) { "Ese horario ya no está libre." }
        require(d.modalidad in t.modalidades && d.formato in t.formatos) { "El tutor no ofrece esa modalidad." }
        require(d.materia in t.materias) { "El tutor no atiende esa materia." }
        require(d.tema.isNotBlank() && d.objetivo.trim().length >= 20) { "Indica el tema y un objetivo de al menos 20 caracteres." }
        val companeros = if (d.formato == Formato.GRUPAL) d.companeros.map { it.trim().lowercase() } else emptyList()
        if (d.formato == Formato.GRUPAL) {
            require(
                companeros.size in 1..4 && companeros.all(::esInstitucional) &&
                    companeros.distinct().size == companeros.size && u.correo !in companeros,
            ) { "Revisa los correos de tus compañeros." }
        }
        val estado = when {
            inicio == null -> Estado.LISTA_ESPERA
            t.confirmaAutomatico -> Estado.CONFIRMADA
            else -> Estado.PENDIENTE
        }
        val adjunto = d.archivoBase64?.let {
            runCatching { Base64.getDecoder().decode(it) }.getOrElse { throw IllegalArgumentException("El archivo llegó dañado.") }
        }
        require((d.archivo == null) == (adjunto == null) && (d.archivo == null || d.archivo.length in 1..255)) { "Falta el archivo adjunto." }
        require(adjunto == null || adjunto.size <= MAX_ARCHIVO) { "El archivo pesa más de 10 MB." }
        val enEspera = solicitudes.countByTutorIdAndEstado(t.id, Estado.LISTA_ESPERA)
        val s = Solicitud(
            d.id, t, u, d.tipo, d.modalidad, d.formato, companeros.toMutableList(), d.materia, d.tema.trim(),
            d.objetivo.trim(), d.archivo, inicio, estado,
            nota = if (estado == Estado.LISTA_ESPERA) "posición ${enEspera + 1} en la lista" else null,
            creadaEn = reloj.ahora().atZone(Reloj.ZONA).toInstant(),
            pendienteDesde = if (estado == Estado.PENDIENTE) reloj.ahora() else null,
        )
        // Flush antes del INSERT de archivos: su llave foránea necesita la solicitud.
        return solicitudes.saveAndFlush(s).also { guardada ->
            adjunto?.let { jdbc.update("INSERT INTO archivos (solicitud_id, datos) VALUES (?, ?)", guardada.id, it) }
        }.dto()
    }

    fun cancelar(yo: Long, id: String) = propia(yo, id).let { s ->
        require(s.activa || s.estado == Estado.LISTA_ESPERA) { "Esta solicitud ya no se puede cancelar." }
        s.estado = Estado.CANCELADA
        s.nota = "Cancelada por ti"
        s.dto()
    }

    fun cambiarHorario(yo: Long, id: String, nuevo: LocalDateTime) = propia(yo, id).let { s ->
        // Desde la lista de espera es elegir el horario que se liberó.
        require(s.activa || s.estado == Estado.LISTA_ESPERA) { "Esta solicitud ya no se puede cambiar." }
        require(nuevo in horariosLibres(s.tutor)) { "Ese horario ya no está libre." }
        s.inicio = nuevo
        s.estado = if (s.tutor.confirmaAutomatico) Estado.CONFIRMADA else Estado.PENDIENTE
        s.pendienteDesde = reloj.ahora().takeIf { s.estado == Estado.PENDIENTE }
        s.nota = null
        s.dto()
    }

    fun solicitarCambio(yo: Long, id: String, mensaje: String) = propia(yo, id).let { s ->
        require(s.activa) { "Esta solicitud ya no se puede cambiar." }
        require(mensaje.isNotBlank()) { "Escribe el motivo del cambio." }
        s.estado = Estado.PENDIENTE
        s.pendienteDesde = reloj.ahora()
        s.nota = "Cambio solicitado: ${mensaje.trim()}"
        s.dto()
    }

    fun permitirCoordinacion(yo: Long, id: String, permitir: Boolean) = propia(yo, id).let { s ->
        s.coordinacionPuedeVer = permitir
        s.dto()
    }

    fun aceptar(yo: Long, id: String) = deMiTutoria(yo, id).let { s ->
        require(s.estado == Estado.PENDIENTE) { "Solo se pueden aceptar solicitudes pendientes." }
        s.estado = Estado.CONFIRMADA
        s.nota = null
        s.dto()
    }

    fun rechazar(yo: Long, id: String, motivo: String) = deMiTutoria(yo, id).let { s ->
        require(s.estado == Estado.PENDIENTE) { "Solo se pueden rechazar solicitudes pendientes." }
        require(motivo.isNotBlank()) { "Indica el motivo del rechazo." }
        s.estado = Estado.RECHAZADA
        s.nota = "Motivo: ${motivo.trim()}"
        s.dto()
    }

    fun cancelarComoTutor(yo: Long, id: String, motivo: String) = deMiTutoria(yo, id).let { s ->
        require(s.estado == Estado.CONFIRMADA && s.inicio?.isAfter(reloj.ahora()) == true) {
            "Solo se pueden cancelar sesiones confirmadas que todavía no ocurren."
        }
        require(motivo.isNotBlank()) { "Indica el motivo de la cancelación." }
        s.estado = Estado.CANCELADA
        s.nota = "Cancelada por el tutor. Motivo: ${motivo.trim()}"
        s.dto()
    }

    fun registrarSesion(yo: Long, id: String, asistio: Boolean, notas: String) = deMiTutoria(yo, id).let { s ->
        require(s.estado == Estado.CONFIRMADA) { "Solo se pueden registrar sesiones confirmadas." }
        require(s.inicio?.isBefore(reloj.ahora()) == true) { "La sesión todavía no ocurre." }
        s.estado = Estado.COMPLETADA
        s.asistio = asistio
        s.notasSesion = notas.trim().ifEmpty { null }
        s.dto()
    }

    fun guardarSemana(yo: Long, tutorId: String, lunes: LocalDate, abiertos: Set<LocalDateTime>) = miTutor(yo, tutorId).let { t ->
        require(lunes.dayOfWeek == DayOfWeek.MONDAY) { "La semana debe empezar en lunes." }
        val ahora = reloj.ahora()
        val editable = { h: LocalDateTime -> h.isAfter(ahora) && h.toLocalDate() in lunes..lunes.plusDays(4) }
        require(abiertos.all(editable)) { "Solo se pueden abrir horas futuras de esa semana." }
        require(t.horarios.none { editable(it) && ocupado(t.id, it) && it !in abiertos }) { "Ese horario ya tiene una sesión agendada." }
        t.horarios.removeIf(editable)
        t.horarios.addAll(abiertos)
        t.dto()
    }

    fun copiarSemana(yo: Long, tutorId: String, lunes: LocalDate): TutorDto {
        require(lunes.dayOfWeek == DayOfWeek.MONDAY) { "La semana debe empezar en lunes." }
        val t = miTutor(yo, tutorId)
        val siguiente = lunes.plusWeeks(1)
        val copia = t.horarios.filter { it.toLocalDate() in lunes..lunes.plusDays(4) }.map { it.plusWeeks(1) }
        val agendadas = t.horarios.filter { it.toLocalDate() in siguiente..siguiente.plusDays(4) && ocupado(t.id, it) }
        return guardarSemana(yo, tutorId, siguiente, (copia + agendadas).filter { it.isAfter(reloj.ahora()) }.toSet())
    }

    fun agregarMateria(yo: Long, tutorId: String, materia: String) = miTutor(yo, tutorId).let { t ->
        val m = materia.trim()
        require(m.isNotEmpty() && m.length <= 150) { "Escribe el nombre de la materia." }
        require(t.materias.none { it.equals(m, ignoreCase = true) }) { "Ya atiendes esa materia." }
        t.materias += m
        t.dto()
    }

    fun quitarMateria(yo: Long, tutorId: String, materia: String) = miTutor(yo, tutorId).let { t ->
        require(t.materias.size > 1) { "Debes atender al menos una materia." }
        t.materias -= materia
        t.dto()
    }

    fun ajustarModalidades(yo: Long, tutorId: String, modalidades: Set<Modalidad>, formatos: Set<Formato>, confirmaAutomatico: Boolean) =
        miTutor(yo, tutorId).let { t ->
            require(modalidades.isNotEmpty()) { "Elige al menos una modalidad." }
            require(formatos.isNotEmpty()) { "Elige al menos un formato." }
            t.modalidades.clear(); t.modalidades += modalidades
            t.formatos.clear(); t.formatos += formatos
            t.confirmaAutomatico = confirmaAutomatico
            t.dto()
        }

    /**
     * El tutor tiene [ANTICIPACION_HORAS] para responder, y nunca después del horario pedido.
     * Sin respuesta, la solicitud se rechaza y el horario queda libre.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    fun vencerPendientes() {
        val ahora = reloj.ahora()
        solicitudes.findByEstado(Estado.PENDIENTE).forEach { s ->
            val desde = s.pendienteDesde ?: LocalDateTime.ofInstant(s.creadaEn, Reloj.ZONA)
            val limite = listOfNotNull(desde.plusHours(ANTICIPACION_HORAS), s.inicio).min()
            if (!ahora.isBefore(limite)) {
                s.estado = Estado.RECHAZADA
                s.nota = "Motivo: sin respuesta del tutor"
            }
        }
    }

    /** Nombre y contenido del archivo adjunto. Lo ven el estudiante, su tutor y Coordinación si tiene permiso. */
    @Transactional(readOnly = true)
    fun archivo(yo: Long, id: String): Pair<String, ByteArray> {
        val s = solicitud(id)
        val puede = s.estudiante.id == yo || s.tutor.usuario?.id == yo || (usuario(yo).rol == Rol.COORDINACION && s.coordinacionPuedeVer)
        if (!puede) prohibido()
        val datos = jdbc.query("SELECT datos FROM archivos WHERE solicitud_id = ?", { rs, _ -> rs.getBytes(1) }, id).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return checkNotNull(s.archivo) to datos
    }

    fun marcarLeidos(yo: Long, ids: List<String>) {
        require(ids.size <= 500 && ids.all { it.length <= 80 }) { "Demasiados avisos." }
        usuario(yo).leidos += ids
    }

    @Transactional(readOnly = true)
    fun mensajes(yo: Long, id: String, despuesDe: Long): List<MensajeDto> {
        participante(yo, id)
        return mensajes.findBySolicitudIdAndIdGreaterThanOrderById(id, despuesDe).map { it.dto(yo) }
    }

    fun enviarMensaje(yo: Long, id: String, texto: String): MensajeDto {
        val s = participante(yo, id)
        require(texto.isNotBlank() && texto.length <= 2000) { "El mensaje debe tener entre 1 y 2000 caracteres." }
        return mensajes.save(Mensaje(solicitud = s, autor = usuario(yo), texto = texto.trim())).dto(yo)
    }

    private fun horariosLibres(t: Tutor): List<LocalDateTime> {
        val limite = reloj.ahora().plusHours(ANTICIPACION_HORAS)
        return t.horarios.filter { it.isAfter(limite) && !ocupado(t.id, it) }
    }

    private fun ocupado(tutorId: String, h: LocalDateTime) = solicitudes.existsByTutorIdAndInicioAndEstadoIn(tutorId, h, ACTIVAS)

    private fun usuario(id: Long): Usuario = usuarios.findById(id).orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED) }

    private fun tutor(id: String): Tutor = tutores.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    private fun solicitud(id: String): Solicitud = solicitudes.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    /** Solicitud del estudiante que hace la petición. */
    private fun propia(yo: Long, id: String) = solicitud(id).also { if (it.estudiante.id != yo) prohibido() }

    /** Solicitud dirigida al tutor que hace la petición. */
    private fun deMiTutoria(yo: Long, id: String) = solicitud(id).also { if (it.tutor.usuario?.id != yo) prohibido() }

    private fun participante(yo: Long, id: String) =
        solicitud(id).also { if (it.estudiante.id != yo && it.tutor.usuario?.id != yo) prohibido() }

    private fun miTutor(yo: Long, tutorId: String) = tutor(tutorId).also { if (it.usuario?.id != yo) prohibido() }

    private fun prohibido(): Nothing = throw ResponseStatusException(HttpStatus.FORBIDDEN)

    companion object {
        const val ANTICIPACION_HORAS = 24L
        const val MAX_ARCHIVO = 10 * 1024 * 1024
        val ACTIVAS = listOf(Estado.PENDIENTE, Estado.CONFIRMADA)

        fun esInstitucional(correo: String): Boolean {
            val arroba = correo.indexOf('@')
            return arroba > 0 && correo.substring(arroba) in setOf("@uabcs.mx", "@alu.uabcs.mx")
        }
    }
}

fun Tutor.dto(ocupadosAjenos: Set<LocalDateTime> = emptySet()) = TutorDto(
    id, nombre, titulo, area, resumen, materias.toList(), calificacion, sesiones, modalidades.toSet(), formatos.toSet(),
    lugar, confirmaAutomatico, experiencia.toList(), (horarios - ocupadosAjenos).sorted(),
    usuario?.takeIf { it.mostrarCorreo }?.correo,
)

private fun AvisoInstitucional.dto() = AvisoInstitucionalDto(id, titulo, detalle, hasta)

/** [privada]: Coordinación sin permiso del estudiante solo ve fecha, tutor y asistencia. */
fun Solicitud.dto(privada: Boolean = false) = SolicitudDto(
    id, tutor.id, tipo, modalidad, formato,
    if (privada) emptyList() else companeros.toList(),
    materia,
    if (privada) "" else tema,
    if (privada) "" else objetivo,
    if (privada) null else archivo,
    inicio, estado, nota, coordinacionPuedeVer, asistio,
    if (privada) null else notasSesion,
    if (privada) null else EstudianteDto(estudiante.correo, estudiante.nombre, estudiante.detalle),
    LocalDateTime.ofInstant(creadaEn, Reloj.ZONA),
)

private fun Mensaje.dto(yo: Long) = MensajeDto(id!!, autor.nombre, autor.id == yo, texto, enviadoEn)
