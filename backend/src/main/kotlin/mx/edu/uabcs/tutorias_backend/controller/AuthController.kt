package mx.edu.uabcs.tutorias_backend.controller

import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import mx.edu.uabcs.tutorias_backend.security.IntentosLogin
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit

data class LoginRequest(val correo: String, val password: String)
data class LoginResponse(val token: String, val nombre: String, val rol: Rol)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val usuarios: UsuarioRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtEncoder,
    private val intentos: IntentosLogin,
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

        val ahora = Instant.now()
        val claims = JwtClaimsSet.builder()
            .subject(u.id.toString())
            .claim("scope", u.rol.name) // queda como autoridad SCOPE_TUTOR, etc.
            .issuedAt(ahora)
            .expiresAt(ahora.plus(8, ChronoUnit.HOURS))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build() // sin esto el encoder intenta RS256 y falla
        return LoginResponse(jwt.encode(JwtEncoderParameters.from(header, claims)).tokenValue, u.nombre, u.rol)
    }

    @GetMapping("/yo")
    fun yo(@AuthenticationPrincipal token: Jwt): Map<String, String?> =
        mapOf("id" to token.subject, "rol" to token.getClaimAsString("scope"))
}