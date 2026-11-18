package mx.edu.uabcs.tutorias_backend.config

import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.entity.Tutor
import mx.edu.uabcs.tutorias_backend.entity.AvisoInstitucional
import mx.edu.uabcs.tutorias_backend.entity.Usuario
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad
import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.repository.AvisoInstitucionalRepository
import mx.edu.uabcs.tutorias_backend.repository.TutorRepository
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

/** Cuentas y tutores de prueba, los mismos que usa la app sin conexión. Solo se crean los que faltan. */
@Configuration
@Profile("dev")
class DatosDemo {
    @Bean
    fun sembrar(usuarios: UsuarioRepository, tutores: TutorRepository, avisos: AvisoInstitucionalRepository, encoder: PasswordEncoder, @Value("\${DEMO_PASSWORD}") pass: String) =
        CommandLineRunner {
            val hash = checkNotNull(encoder.encode(pass))
            fun cuenta(nombre: String, correo: String, rol: Rol, detalle: String) =
                usuarios.findByCorreo(correo).orElseGet {
                    usuarios.save(Usuario(nombre = nombre, correo = correo, passwordHash = hash, rol = rol, detalle = detalle))
                }

            cuenta("Derrick Ramírez", "derrick.ramirez@alu.uabcs.mx", Rol.ESTUDIANTE, "Ing. en Desarrollo de Software · 7.º semestre")
            cuenta("Mtra. Laura Peña", "lpena@uabcs.mx", Rol.COORDINACION, "Coordinación de Tutorías")
            if (avisos.count() == 0L) {
                avisos.save(
                    AvisoInstitucional(
                        "demo-grupales", "Nuevo: sesiones grupales", "Invita hasta 4 compañeros a tu asesoría.",
                        LocalDate.now(Reloj.ZONA).plusMonths(2),
                    ),
                )
            }

            val hoy = LocalDate.now(Reloj.ZONA)
            fun agenda(vararg dias: Pair<DayOfWeek, List<Int>>): List<LocalDateTime> {
                val porDia = dias.toMap()
                return (0L until 14L).flatMap { n -> hoy.plusDays(n).let { f -> porDia[f.dayOfWeek].orEmpty().map { f.atTime(it, 0) } } }
            }
            fun tutor(
                id: String, correo: String, nombre: String, titulo: String, area: String, departamento: String, resumen: String,
                materias: List<String>, calificacion: Double, sesiones: Int, modalidades: Set<Modalidad>, formatos: Set<Formato>,
                lugar: String, auto: Boolean, experiencia: List<String>, horarios: List<LocalDateTime>,
            ) {
                if (tutores.existsById(id)) return
                tutores.save(
                    Tutor(
                        id, cuenta(nombre, correo, Rol.TUTOR, departamento), nombre, titulo, area, resumen, calificacion, sesiones, lugar, auto,
                        materias.toMutableList(), experiencia.toMutableList(), modalidades.toMutableSet(), formatos.toMutableSet(), horarios.toMutableSet(),
                    ),
                )
            }

            val ambas = setOf(Modalidad.PRESENCIAL, Modalidad.EN_LINEA)
            tutor(
                "mariana", "mcota@uabcs.mx", "Mariana Cota", "Tutora", "Ing. en Desarrollo de Software", "Departamento Académico de Matemáticas",
                "Matemáticas · Cálculo · Álgebra", listOf("Cálculo diferencial", "Cálculo integral", "Álgebra lineal", "Preparación de exámenes"),
                4.9, 32, ambas, setOf(Formato.INDIVIDUAL, Formato.GRUPAL), "Biblioteca Central, cubículo 3", true,
                listOf("Maestría en Matemática Educativa (en curso)", "5 años como asesora par en Ciencias Exactas", "Ganadora del concurso estatal de Matemáticas 2024"),
                agenda(MONDAY to listOf(17), TUESDAY to listOf(11), WEDNESDAY to listOf(9, 16), THURSDAY to listOf(16, 17), FRIDAY to listOf(18)),
            )
            tutor(
                "sofia", "samador@uabcs.mx", "Sofía Amador", "Tutora", "Lic. en Economía", "Departamento Académico de Economía",
                "Matemáticas · Estadística · Probabilidad", listOf("Estadística descriptiva", "Probabilidad", "Matemáticas financieras"),
                4.7, 18, setOf(Modalidad.PRESENCIAL), setOf(Formato.INDIVIDUAL), "Edificio de Economía, aula 12", false,
                listOf("Ayudante de investigación en el Departamento de Economía", "3 semestres como asesora de Estadística"),
                agenda(TUESDAY to listOf(11), THURSDAY to listOf(15), SATURDAY to listOf(10)),
            )
            tutor(
                "luis", "lgeraldo@uabcs.mx", "Luis Geraldo", "Tutor", "Ing. en Desarrollo de Software", "Departamento Académico de Sistemas Computacionales",
                "Programación · Cálculo · Bases de datos", listOf("Programación I", "Estructuras de datos", "Bases de datos", "Cálculo diferencial"),
                4.8, 25, ambas, setOf(Formato.GRUPAL), "Laboratorio de Cómputo 2", false,
                listOf("Desarrollador en el Laboratorio de Software UABCS", "Instructor del taller de Python 2025"),
                agenda(MONDAY to listOf(17), WEDNESDAY to listOf(9), FRIDAY to listOf(9)),
            )
            tutor(
                "ruben", "rcastro@uabcs.mx", "Rubén Castro Higuera", "Tutor", "Ing. en Geología", "Departamento Académico de Ciencias de la Tierra",
                "Física · Mecánica · Cálculo", listOf("Física I", "Mecánica clásica", "Cálculo integral"),
                4.6, 11, setOf(Modalidad.PRESENCIAL), setOf(Formato.INDIVIDUAL, Formato.GRUPAL), "Edificio AD, cubículo 7", false,
                listOf("Profesor de asignatura en Ciencias de la Tierra"), emptyList(),
            )
        }
}
