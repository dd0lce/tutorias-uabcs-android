package mx.edu.uabcs.tutorias_backend.controller

import mx.edu.uabcs.tutorias_backend.entity.Usuario
import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.repository.TutorRepository
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import mx.edu.uabcs.tutorias_backend.security.IntentosLogin
import mx.edu.uabcs.tutorias_backend.service.Codigos
import mx.edu.uabcs.tutorias_backend.service.ServicioTutorias
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit

data class LoginRequest(val correo: String, val password: String)
data class UsuarioDto(val nombre: String, val correo: String, val rol: Rol, val detalle: String, val tutorId: String?, val mostrarCorreo: Boolean)
data class PerfilRequest(val nombre: String, val detalle: String, val mostrarCorreo: Boolean = false)
data class LoginResponse(val token: String, val usuario: UsuarioDto)
data class CorreoRequest(val correo: String)
data class RegistroRequest(val correo: String, val codigo: String, val nombre: String, val detalle: String, val password: String)
data class RecuperarRequest(val correo: String, val codigo: String, val password: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val usuarios: UsuarioRepository,
    private val tutores: TutorRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtEncoder,
    private val intentos: IntentosLogin,
    private val codigos: Codigos,
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): LoginResponse {
        val correo = req.correo.trim().lowercase()
        if (intentos.bloqueado(correo)) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos. Espera 15 minutos.")
        }

        val u = usuarios.findByCorreo(correo).orElse(null)
            ?.takeIf { it.activo && encoder.matches(req.password, it.passwordHash) }
            ?: run {
                intentos.fallo(correo)
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos")
            }
        intentos.exito(correo)
        return emitir(u)
    }

    @GetMapping("/yo")
    fun yo(@AuthenticationPrincipal token: Jwt): UsuarioDto =
        usuarios.findById(checkNotNull(token.subject).toLong()).map(::dto).orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED) }

    @PostMapping("/perfil")
    @Transactional
    fun perfil(@AuthenticationPrincipal token: Jwt, @RequestBody req: PerfilRequest): UsuarioDto {
        val u = usuarios.findById(checkNotNull(token.subject).toLong()).orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED) }
        val nombre = req.nombre.trim()
        require(nombre.length in 3..150) { "Escribe tu nombre completo." }
        require(req.detalle.trim().length <= 200) { "El detalle es demasiado largo." }
        u.nombre = nombre
        u.detalle = req.detalle.trim()
        u.mostrarCorreo = u.rol == Rol.TUTOR && req.mostrarCorreo
        tutores.findByUsuarioId(checkNotNull(u.id))?.nombre = nombre
        return dto(u)
    }

    /** Solo los estudiantes se registran solos; tutores y Coordinación los da de alta la universidad. */
    @PostMapping("/registro/codigo")
    fun codigoRegistro(@RequestBody req: CorreoRequest) {
        val correo = req.correo.trim().lowercase()
        require(correo.endsWith("@alu.uabcs.mx") && ServicioTutorias.esInstitucional(correo)) {
            "Solo los estudiantes con correo @alu.uabcs.mx pueden registrarse."
        }
        require(!usuarios.existsByCorreo(correo)) { "Ese correo ya tiene cuenta. Inicia sesión o recupera tu contraseña." }
        codigos.enviar(correo, REGISTRO)
    }

    @PostMapping("/registro")
    @Transactional
    fun registro(@RequestBody req: RegistroRequest): LoginResponse {
        val correo = req.correo.trim().lowercase()
        val nombre = req.nombre.trim()
        require(nombre.length in 3..150) { "Escribe tu nombre completo." }
        require(req.detalle.trim().length <= 200) { "La carrera y el semestre son demasiado largos." }
        validarContrasena(req.password)
        require(!usuarios.existsByCorreo(correo)) { "Ese correo ya tiene cuenta." }
        codigos.verificar(correo, REGISTRO, req.codigo)
        val u = usuarios.save(
            Usuario(nombre = nombre, correo = correo, passwordHash = checkNotNull(encoder.encode(req.password)), rol = Rol.ESTUDIANTE, detalle = req.detalle.trim()),
        )
        return emitir(u)
    }

    /** Responde igual exista o no la cuenta, para no revelar qué correos están registrados. */
    @PostMapping("/recuperar/codigo")
    fun codigoRecuperar(@RequestBody req: CorreoRequest) {
        val correo = req.correo.trim().lowercase()
        if (usuarios.existsByCorreo(correo)) codigos.enviar(correo, RECUPERAR)
    }

    @PostMapping("/recuperar")
    @Transactional
    fun recuperar(@RequestBody req: RecuperarRequest) {
        val correo = req.correo.trim().lowercase()
        validarContrasena(req.password)
        codigos.verificar(correo, RECUPERAR, req.codigo)
        val u = usuarios.findByCorreo(correo).orElseThrow { IllegalArgumentException("Código incorrecto.") }
        u.passwordHash = checkNotNull(encoder.encode(req.password))
        intentos.exito(correo)
    }

    private fun validarContrasena(p: String) =
        require(p.length in 8..72) { "La contraseña debe tener entre 8 y 72 caracteres." }

    private fun dto(u: Usuario) = UsuarioDto(u.nombre, u.correo, u.rol, u.detalle, tutores.findByUsuarioId(checkNotNull(u.id))?.id, u.mostrarCorreo)

    private fun emitir(u: Usuario): LoginResponse {
        val ahora = Instant.now()
        val claims = JwtClaimsSet.builder()
            .subject(u.id.toString())
            .claim("scope", u.rol.name) // queda como autoridad SCOPE_TUTOR, etc.
            .issuedAt(ahora)
            .expiresAt(ahora.plus(8, ChronoUnit.HOURS))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build() // sin esto el encoder intenta RS256 y falla
        return LoginResponse(jwt.encode(JwtEncoderParameters.from(header, claims)).tokenValue, dto(u))
    }

    companion object {
        const val REGISTRO = "REGISTRO"
        const val RECUPERAR = "RECUPERAR"
    }
}
