package mx.uabcs.tutorias.data

import androidx.compose.runtime.mutableStateListOf
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.LocalDateTime

enum class Modalidad(val nombre: String) { PRESENCIAL("Presencial"), EN_LINEA("En línea") }

enum class Formato(val nombre: String) { INDIVIDUAL("Individual"), GRUPAL("Grupal") }

enum class TipoSesion(val nombre: String, val descripcion: String) {
    MATERIA("Asesoría de materia", "Resuelve dudas de un tema o ejercicio."),
    EXAMEN("Preparación de exámenes", "Repasa antes de un parcial u ordinario."),
    TRABAJOS("Orientación para trabajos", "Avances de tareas, proyectos o tesis."),
    TRAYECTORIA("Tutoría de trayectoria", "Plan de estudios, carga académica, servicio social."),
}

enum class Estado(val nombre: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada"),
    LISTA_ESPERA("Lista de espera"),
    COMPLETADA("Completada"),
}

enum class Franja(val nombre: String, val horas: IntRange) {
    MANANA("Mañana 7–12", 7..11),
    TARDE("Tarde 12–18", 12..17),
    NOCHE("Noche 18–21", 18..20),
}

data class Tutor(
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
) {
    val iniciales: String
        get() = nombre.split(" ").filter {it.firstOrNull()?.isUpperCase() == true}.take(2).joinToString("") { it.take(1)}
    }

data class Solicitud(
    val id: String,
    val tutorId: String,
    val tipo: TipoSesion,
    val modalidad: Modalidad,
    val formato: Formato,
    val companeros: List<String>,
    val materia: String,
    val tema: String,
    val objetivo: String,
    val archivo: String?,
    val inicio: LocalDateTime?, 
    val estado: Estado,
    val nota: String? = null,
    val coordinacionPuedeVer: Boolean = false,
) {
    val titulo: String
        get() = when (tipo) {
            TipoSesion.MATERIA -> "Asesoría de $materia"
            TipoSesion.EXAMEN -> "Preparación de examen de $materia"
            TipoSesion.TRABAJOS -> "Orientación para $tema"
            TipoSesion.TRAYECTORIA -> "Tutoría de trayectoria"
        }
    val activa: Boolean get() = estado == Estado.PENDIENTE || estado == Estado.CONFIRMADA
}

data class Filtros(
    val texto: String = "",
    val modalidades: Set<Modalidad> = emptySet(),
    val dias: Set<DayOfWeek> = emptySet(),
    val franjas: Set<Franja> = emptySet(),
) {
    val activos: Int get() = modalidades.size + dias.size + franjas.size
}

class TutoriasRepo(private val reloj: () -> LocalDateTime) {
    private val hoy: LocalDate = reloj().toLocalDate()

    val tutores: List<Tutor> = listOf(
        Tutor(
            "mariana", "Mariana Cota", "Tutora", "Ing. en Desarrollo de Software", "Matemáticas · Cálculo · Álgebra",
            listOf("Cálculo diferencial", "Cálculo integral", "Álgebra lineal", "Preparación de exámenes"), 4.9, 32,
            setOf(Modalidad.PRESENCIAL, Modalidad.EN_LINEA), setOf(Formato.INDIVIDUAL, Formato.GRUPAL),
            "Biblioteca Central, cubículo 3", confirmaAutomatico = true,
            listOf(
                "Maestría en Matemática Educativa (en curso)",
                "5 años como asesora par en Ciencias Exactas",
                "Ganadora del concurso estatal de Matemáticas 2024",
            ),
            agenda(MONDAY to listOf(17), TUESDAY to listOf(11), WEDNESDAY to listOf(9, 16), THURSDAY to listOf(16, 17), FRIDAY to listOf(18)),
        ),
        Tutor(
            "sofia", "Sofía Amador", "Tutora", "Lic. en Economía", "Matemáticas · Estadística · Probabilidad",
            listOf("Estadística descriptiva", "Probabilidad", "Matemáticas financieras"), 4.7, 18,
            setOf(Modalidad.PRESENCIAL), setOf(Formato.INDIVIDUAL),
            "Edificio de Economía, aula 12", confirmaAutomatico = false,
            listOf("Ayudante de investigación en el Departamento de Economía", "3 semestres como asesora de Estadística"),
            agenda(TUESDAY to listOf(11), THURSDAY to listOf(15), SATURDAY to listOf(10)),
        ),
        Tutor(
            "luis", "Luis Geraldo", "Tutor", "Ing. en Desarrollo de Software", "Programación · Cálculo · Bases de datos",
            listOf("Programación I", "Estructuras de datos", "Bases de datos", "Cálculo diferencial"), 4.8, 25,
            setOf(Modalidad.PRESENCIAL, Modalidad.EN_LINEA), setOf(Formato.GRUPAL),
            "Laboratorio de Cómputo 2", confirmaAutomatico = false,
            listOf("Desarrollador en el Laboratorio de Software UABCS", "Instructor del taller de Python 2025"),
            agenda(MONDAY to listOf(17), WEDNESDAY to listOf(9), FRIDAY to listOf(9)),
        ),
        Tutor(
            "ruben", "Rubén Castro Higuera", "Tutor", "Ing. en Geología", "Física · Mecánica · Cálculo",
            listOf("Física I", "Mecánica clásica", "Cálculo integral"), 4.6, 11,
            setOf(Modalidad.PRESENCIAL), setOf(Formato.INDIVIDUAL, Formato.GRUPAL),
            "Edificio AD, cubículo 7", confirmaAutomatico = false,
            listOf("Profesor de asignatura en Ciencias de la Tierra"),
            emptyList(),
        ),
    )

    val solicitudes = mutableStateListOf<Solicitud>()
    private var siguienteId = 1

    init {
        val mariana = horariosLibres(tutor("mariana"))
        val luis = horariosLibres(tutor("luis"))
        solicitudes += Solicitud(
            nuevoId(), "mariana", TipoSesion.MATERIA, Modalidad.PRESENCIAL, Formato.INDIVIDUAL, emptyList(),
            "Matemáticas", "Límites y continuidad",
            "Entender cómo resolver límites al infinito y practicar los ejercicios 4 a 8 de la guía.",
            "Guia_limites.pdf", mariana.first(), Estado.CONFIRMADA,
        )
        solicitudes += Solicitud(
            nuevoId(), "luis", TipoSesion.MATERIA, Modalidad.EN_LINEA, Formato.GRUPAL,
            listOf("ana.lucero@alu.uabcs.mx", "pojeda@alu.uabcs.mx"),
            "Programación", "Ciclos anidados en Python", "Repasar ciclos anidados antes de entregar la práctica 3.",
            null, luis.first(), Estado.PENDIENTE,
        )
        solicitudes += Solicitud(
            nuevoId(), "sofia", TipoSesion.EXAMEN, Modalidad.PRESENCIAL, Formato.INDIVIDUAL, emptyList(),
            "Estadística", "Distribución normal", "Repasar tablas Z y los ejercicios del segundo parcial.",
            null, null, Estado.LISTA_ESPERA, "posición 2 en la lista",
        )
        solicitudes += Solicitud(
            nuevoId(), "luis", TipoSesion.TRABAJOS, Modalidad.EN_LINEA, Formato.GRUPAL, emptyList(),
            "Bases de datos", "proyecto final", "Revisar el modelo entidad-relación del proyecto final.",
            null, luis.getOrNull(1), Estado.RECHAZADA, "Motivo: horario no disponible",
        )
        solicitudes += Solicitud(
            nuevoId(), "ruben", TipoSesion.MATERIA, Modalidad.PRESENCIAL, Formato.INDIVIDUAL, emptyList(),
            "Física", "Tiro parabólico", "Resolver los problemas de la tarea 2.",
            null, hoy.minusDays(3).atTime(12, 0), Estado.CANCELADA, "Cancelada por ti",
        )
    }

    fun tutor(id: String): Tutor = tutores.first { it.id == id }

    fun solicitud(id: String): Solicitud? = solicitudes.firstOrNull { it.id == id }

    fun horariosLibres(t: Tutor): List<LocalDateTime> {
        val limite = reloj().plusHours(ANTICIPACION_HORAS)
        return t.horarios.filter { h ->
            h.isAfter(limite) && solicitudes.none { it.tutorId == t.id && it.inicio == h && it.activa }
        }
    }

    fun buscar(f: Filtros): List<Tutor> {
        val q = normalizar(f.texto.trim())
        return tutores.filter { t ->
            val coincideTexto = q.isEmpty() ||
                normalizar((listOf(t.nombre, t.area, t.resumen) + t.materias).joinToString(" ")).contains(q)
            val coincideModalidad = f.modalidades.isEmpty() || t.modalidades.any { it in f.modalidades }
            val coincideHorario = (f.dias.isEmpty() && f.franjas.isEmpty()) || horariosLibres(t).any { h ->
                (f.dias.isEmpty() || h.dayOfWeek in f.dias) && (f.franjas.isEmpty() || f.franjas.any { h.hour in it.horas })
            }
            coincideTexto && coincideModalidad && coincideHorario
        }.sortedBy { horariosLibres(it).firstOrNull() ?: LocalDateTime.MAX }
    }

    fun parecidos(t: Tutor): List<Tutor> {
        val areas = t.resumen.split(" · ").toSet()
        return tutores.filter { o ->
            o.id != t.id && o.resumen.split(" · ").any { it in areas } && horariosLibres(o).isNotEmpty()
        }
    }

    fun proximas(): List<Solicitud> =
        solicitudes.filter { it.activa && it.inicio?.isAfter(reloj()) == true }.sortedBy { it.inicio }

    fun cuantas(estado: Estado): Int = solicitudes.count { it.estado == estado }

    fun enviar(borrador: Solicitud): Solicitud {
        val t = tutor(borrador.tutorId)
        val inicio = borrador.inicio
        require(inicio == null || inicio in horariosLibres(t)) { "Ese horario ya no está libre." }
        val estado = when {
            inicio == null -> Estado.LISTA_ESPERA
            t.confirmaAutomatico -> Estado.CONFIRMADA
            else -> Estado.PENDIENTE
        }
        val enEspera = solicitudes.count { it.tutorId == t.id && it.estado == Estado.LISTA_ESPERA }
        val nueva = borrador.copy(
            id = nuevoId(),
            estado = estado,
            nota = if (estado == Estado.LISTA_ESPERA) "posición ${enEspera + 1} en la lista" else null,
        )
        solicitudes.add(0, nueva)
        return nueva
    }

    fun cancelar(id: String) = actualizar(id) { it.copy(estado = Estado.CANCELADA, nota = "Cancelada por ti") }

    fun cambiarHorario(id: String, nuevo: LocalDateTime) = actualizar(id) { s ->
        val t = tutor(s.tutorId)
        require(nuevo in horariosLibres(t)) { "Ese horario ya no está libre." }
        s.copy(inicio = nuevo, estado = if (t.confirmaAutomatico) Estado.CONFIRMADA else Estado.PENDIENTE, nota = null)
    }

    fun solicitarCambio(id: String, mensaje: String) =
        actualizar(id) { it.copy(estado = Estado.PENDIENTE, nota = "Cambio solicitado: ${mensaje.trim()}") }

    fun aceptar(id: String) = actualizar(id) { s ->
        require(s.estado == Estado.PENDIENTE) { "Solo se pueden aceptar solicitudes pendientes." }
        s.copy(estado = Estado.CONFIRMADA, nota = null)
    }

    fun rechazar(id: String, motivo: String) = actualizar(id) { s ->
        require(s.estado == Estado.PENDIENTE) { "Solo se pueden rechazar solicitudes pendientes." }
        require(motivo.isNotBlank()) { "Indica el motivo del rechazo." }
        s.copy(estado = Estado.RECHAZADA, nota = "Motivo: ${motivo.trim()}")
    }

    fun permitirCoordinacion(id: String, permitir: Boolean) = actualizar(id) { it.copy(coordinacionPuedeVer = permitir) }

    private fun actualizar(id: String, cambio: (Solicitud) -> Solicitud) {
        val i = solicitudes.indexOfFirst { it.id == id }
        require(i >= 0) { "No existe la solicitud $id" }
        solicitudes[i] = cambio(solicitudes[i])
    }

    private fun nuevoId() = "S${siguienteId++}"

    private fun agenda(vararg dias: Pair<DayOfWeek, List<Int>>): List<LocalDateTime> {
        val porDia = dias.toMap()
        return (0L until DIAS_AGENDA).flatMap { n ->
            val fecha = hoy.plusDays(n)
            porDia[fecha.dayOfWeek].orEmpty().map { fecha.atTime(it, 0) }
        }
    }

    private fun normalizar(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()

    companion object {
        const val ANTICIPACION_HORAS = 24L
        const val DIAS_AGENDA = 14L
    }
}

val Tutorias = TutoriasRepo { LocalDateTime.now() }
