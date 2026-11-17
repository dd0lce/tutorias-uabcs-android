package mx.edu.uabcs.tutorias_backend

import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.config.Reloj
import mx.edu.uabcs.tutorias_backend.entity.Tutor
import mx.edu.uabcs.tutorias_backend.entity.Usuario
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad
import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.repository.TutorRepository
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import mx.edu.uabcs.tutorias_backend.service.Correo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest(
    properties = [
        "DB_USERNAME=prueba",
        "DB_PASSWORD=prueba",
        "JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
@Import(ApiTest.CorreoDePrueba::class)
class ApiTest {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        val correos = mutableListOf<String>()

        // Lunes 12 de octubre de 2026, 8:00.
        val AHORA: LocalDateTime = LocalDateTime.of(2026, 10, 12, 8, 0)
        val MIERCOLES_9: LocalDateTime = AHORA.plusDays(2).withHour(9)
        const val PASS = "Tutorias.2026"
    }

    @TestConfiguration
    class CorreoDePrueba {
        @Bean
        @Primary
        fun correoDePrueba(smtp: ObjectProvider<JavaMailSender>, entorno: Environment) =
            object : Correo(smtp, entorno, "prueba@uabcs.mx") {
                override fun enviar(para: String, asunto: String, texto: String) {
                    correos += texto
                }
            }
    }

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var usuarios: UsuarioRepository
    @Autowired lateinit var tutores: TutorRepository
    @Autowired lateinit var encoder: PasswordEncoder
    @Autowired lateinit var reloj: Reloj

    @BeforeEach
    fun datos() {
        jdbc.execute("TRUNCATE usuarios, tutores, solicitudes, mensajes, codigos RESTART IDENTITY CASCADE")
        correos.clear()
        reloj.ahora = { AHORA }
        val hash = checkNotNull(encoder.encode(PASS))
        fun cuenta(correo: String, rol: Rol) = usuarios.save(Usuario(nombre = correo.substringBefore('@'), correo = correo, passwordHash = hash, rol = rol))
        cuenta("ana@alu.uabcs.mx", Rol.ESTUDIANTE)
        cuenta("beto@alu.uabcs.mx", Rol.ESTUDIANTE)
        cuenta("coord@uabcs.mx", Rol.COORDINACION)
        fun tutor(id: String, correo: String, auto: Boolean) = tutores.save(
            Tutor(
                id, cuenta(correo, Rol.TUTOR), id, "Tutor", "Área", "Resumen", 5.0, 0, "Cubículo 1", auto,
                mutableListOf("Cálculo", "Física"), mutableListOf(), mutableSetOf(Modalidad.PRESENCIAL), mutableSetOf(Formato.INDIVIDUAL, Formato.GRUPAL),
                mutableSetOf(MIERCOLES_9, MIERCOLES_9.plusHours(1), AHORA.plusHours(2)),
            ),
        )
        tutor("mariana", "mcota@uabcs.mx", auto = false)
        tutor("luis", "lgeraldo@uabcs.mx", auto = true)
    }

    private fun token(correo: String): String {
        val cuerpo = mvc.perform(json(post("/api/auth/login"), """{"correo":"$correo","password":"$PASS"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        return Regex("\"token\":\"([^\"]+)\"").find(cuerpo)!!.groupValues[1]
    }

    private fun json(b: org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder, cuerpo: String, token: String? = null) =
        b.contentType(MediaType.APPLICATION_JSON).content(cuerpo).also { if (token != null) it.header("Authorization", "Bearer $token") }

    private fun enviar(token: String, id: String, tutor: String = "mariana", inicio: LocalDateTime? = MIERCOLES_9): ResultActions =
        mvc.perform(
            json(
                post("/api/solicitudes"),
                """{"id":"$id","tutorId":"$tutor","tipo":"MATERIA","modalidad":"PRESENCIAL","formato":"INDIVIDUAL",
                   "materia":"Cálculo","tema":"Límites","objetivo":"Practicar límites al infinito con la guía",
                   "inicio":${inicio?.let { "\"$it\"" } ?: "null"}}""",
                token,
            ),
        )

    private fun accion(token: String, ruta: String, cuerpo: String = "{}") = mvc.perform(json(post("/api/$ruta"), cuerpo, token))

    private fun datos(token: String) = mvc.perform(get("/api/datos").header("Authorization", "Bearer $token"))

    @Test
    fun `login devuelve el usuario con su tutorId`() {
        mvc.perform(json(post("/api/auth/login"), """{"correo":" MCOTA@uabcs.mx","password":"$PASS"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario.tutorId").value("mariana"))
            .andExpect(jsonPath("$.usuario.rol").value("TUTOR"))
        mvc.perform(json(post("/api/auth/login"), """{"correo":"mcota@uabcs.mx","password":"otra"}"""))
            .andExpect(status().isUnauthorized)
        mvc.perform(get("/api/datos")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `solicitud pendiente, aceptada por su tutor y oculta para otros estudiantes`() {
        val ana = token("ana@alu.uabcs.mx")
        val beto = token("beto@alu.uabcs.mx")
        val mariana = token("mcota@uabcs.mx")

        enviar(ana, "s1").andExpect(status().isOk).andExpect(jsonPath("$.estado").value("PENDIENTE"))
        enviar(beto, "s2").andExpect(status().isConflict).andExpect(jsonPath("$.mensaje").value("Ese horario ya no está libre."))
        enviar(ana, "s3", inicio = AHORA.plusHours(2)).andExpect(status().isConflict) // menos de 24 h
        enviar(ana, "s4", tutor = "luis").andExpect(jsonPath("$.estado").value("CONFIRMADA")) // confirma solo

        datos(beto)
            .andExpect(jsonPath("$.solicitudes.length()").value(0))
            .andExpect(jsonPath("$.tutores[?(@.id == 'mariana')].horarios[*]", not(hasItem("$MIERCOLES_9:00"))))
        datos(mariana)
            .andExpect(jsonPath("$.solicitudes[0].id").value("s1"))
            .andExpect(jsonPath("$.solicitudes[0].estudiante.correo").value("ana@alu.uabcs.mx"))
            .andExpect(jsonPath("$.solicitudes[0].creadaEn").value("$AHORA:00"))

        accion(ana, "solicitudes/s1/aceptar").andExpect(status().isForbidden)
        accion(mariana, "solicitudes/s4/aceptar").andExpect(status().isForbidden) // es de Luis
        accion(mariana, "solicitudes/s1/aceptar").andExpect(jsonPath("$.estado").value("CONFIRMADA"))
        accion(mariana, "solicitudes/s1/registrar", """{"asistio":true}""").andExpect(status().isConflict) // todavía no ocurre
        accion(ana, "solicitudes/s1/cancelar").andExpect(jsonPath("$.estado").value("CANCELADA"))
        enviar(beto, "s5").andExpect(jsonPath("$.estado").value("PENDIENTE")) // el horario quedó libre
    }

    @Test
    fun `coordinacion solo ve el contenido si el estudiante lo permite`() {
        val ana = token("ana@alu.uabcs.mx")
        val coord = token("coord@uabcs.mx")
        enviar(ana, "s1")
        datos(coord)
            .andExpect(jsonPath("$.solicitudes[0].tema").value(""))
            .andExpect(jsonPath("$.solicitudes[0].estudiante").doesNotExist())
        accion(ana, "solicitudes/s1/coordinacion", """{"permitir":true}""")
        datos(coord)
            .andExpect(jsonPath("$.solicitudes[0].tema").value("Límites"))
            .andExpect(jsonPath("$.solicitudes[0].estudiante.nombre").value("ana"))
        enviar(coord, "s2").andExpect(status().isForbidden)
    }

    @Test
    fun `tutor edita su semana y no la de otros`() {
        val mariana = token("mcota@uabcs.mx")
        val lunes = AHORA.toLocalDate()
        val nuevo = lunes.plusDays(3).atTime(10, 0)
        accion(mariana, "tutores/mariana/semana", """{"lunes":"$lunes","abiertos":["$nuevo"]}""")
            .andExpect(jsonPath("$.horarios[0]").value("$nuevo:00"))
        accion(mariana, "tutores/luis/semana", """{"lunes":"$lunes","abiertos":[]}""").andExpect(status().isForbidden)
        accion(mariana, "tutores/mariana/quitar-materia", """{"materia":"Física"}""").andExpect(jsonPath("$.materias.length()").value(1))
        accion(mariana, "tutores/mariana/quitar-materia", """{"materia":"Cálculo"}""").andExpect(status().isConflict)
    }

    @Test
    fun `mensajes solo entre el estudiante y su tutor`() {
        val ana = token("ana@alu.uabcs.mx")
        val mariana = token("mcota@uabcs.mx")
        val beto = token("beto@alu.uabcs.mx")
        enviar(ana, "s1")
        accion(ana, "solicitudes/s1/mensajes", """{"texto":"¿Llevo la guía?"}""").andExpect(jsonPath("$.propio").value(true))
        mvc.perform(get("/api/solicitudes/s1/mensajes").header("Authorization", "Bearer $mariana"))
            .andExpect(jsonPath("$[0].texto").value("¿Llevo la guía?"))
            .andExpect(jsonPath("$[0].propio").value(false))
        mvc.perform(get("/api/solicitudes/s1/mensajes?despuesDe=1").header("Authorization", "Bearer $mariana"))
            .andExpect(jsonPath("$.length()").value(0))
        accion(beto, "solicitudes/s1/mensajes", """{"texto":"hola"}""").andExpect(status().isForbidden)
    }

    @Test
    fun `avisos leidos se guardan por usuario`() {
        val ana = token("ana@alu.uabcs.mx")
        accion(ana, "avisos/leidos", """{"ids":["s1:CONFIRMADA"]}""").andExpect(status().isOk)
        datos(ana).andExpect(jsonPath("$.leidos[0]").value("s1:CONFIRMADA"))
        datos(token("beto@alu.uabcs.mx")).andExpect(jsonPath("$.leidos.length()").value(0))
    }

    @Test
    fun `registro de estudiante con codigo y recuperacion de contrasena`() {
        val correo = "nuevo@alu.uabcs.mx"
        mvc.perform(json(post("/api/auth/registro/codigo"), """{"correo":"profe@uabcs.mx"}""")).andExpect(status().isConflict)
        mvc.perform(json(post("/api/auth/registro/codigo"), """{"correo":"ana@alu.uabcs.mx"}""")).andExpect(status().isConflict)
        mvc.perform(json(post("/api/auth/registro/codigo"), """{"correo":"$correo"}""")).andExpect(status().isOk)
        val codigo = Regex("\\d{6}").find(correos.single())!!.value
        val registro = """{"correo":"$correo","nombre":"Nuevo Estudiante","detalle":"Derecho · 1.º semestre","password":"secreta123","codigo":"""
        mvc.perform(json(post("/api/auth/registro"), "$registro\"000000\"}")).andExpect(status().isConflict)
        mvc.perform(json(post("/api/auth/registro"), "$registro\"$codigo\"}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario.rol").value("ESTUDIANTE"))
            .andExpect(jsonPath("$.usuario.detalle").value("Derecho · 1.º semestre"))
        mvc.perform(json(post("/api/auth/registro"), "$registro\"$codigo\"}")).andExpect(status().isConflict) // ya se usó

        correos.clear()
        mvc.perform(json(post("/api/auth/recuperar/codigo"), """{"correo":"nadie@uabcs.mx"}""")).andExpect(status().isOk)
        assert(correos.isEmpty())
        mvc.perform(json(post("/api/auth/recuperar/codigo"), """{"correo":"ana@alu.uabcs.mx"}""")).andExpect(status().isOk)
        val otro = Regex("\\d{6}").find(correos.single())!!.value
        mvc.perform(json(post("/api/auth/recuperar"), """{"correo":"ana@alu.uabcs.mx","codigo":"$otro","password":"nueva.clave1"}"""))
            .andExpect(status().isOk)
        mvc.perform(json(post("/api/auth/login"), """{"correo":"ana@alu.uabcs.mx","password":"nueva.clave1"}""")).andExpect(status().isOk)
        mvc.perform(json(post("/api/auth/login"), """{"correo":"ana@alu.uabcs.mx","password":"$PASS"}""")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `el codigo se bloquea despues de 5 intentos`() {
        mvc.perform(json(post("/api/auth/recuperar/codigo"), """{"correo":"ana@alu.uabcs.mx"}"""))
        val bueno = Regex("\\d{6}").find(correos.single())!!.value
        val malo = if (bueno == "000000") "111111" else "000000"
        repeat(5) {
            mvc.perform(json(post("/api/auth/recuperar"), """{"correo":"ana@alu.uabcs.mx","codigo":"$malo","password":"nueva.clave1"}"""))
                .andExpect(status().isConflict)
        }
        mvc.perform(json(post("/api/auth/recuperar"), """{"correo":"ana@alu.uabcs.mx","codigo":"$bueno","password":"nueva.clave1"}"""))
            .andExpect(status().isConflict)
    }
}
