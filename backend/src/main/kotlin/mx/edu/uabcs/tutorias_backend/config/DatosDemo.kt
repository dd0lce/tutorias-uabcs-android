package mx.edu.uabcs.tutorias_backend.config

import mx.edu.uabcs.tutorias_backend.entity.Usuario
import mx.edu.uabcs.tutorias_backend.model.Rol
import mx.edu.uabcs.tutorias_backend.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@Profile("dev")
class DatosDemo {
    @Bean
    fun sembrar(usuarios: UsuarioRepository, encoder: PasswordEncoder, @Value("\${DEMO_PASSWORD}") pass: String) =
        CommandLineRunner {
            val hash = checkNotNull(encoder.encode(pass))
            listOf(
                Triple("Derrick Ramírez", "derrick.ramirez@alu.uabcs.mx", Rol.ESTUDIANTE),
                Triple("Mariana Cota", "mcota@uabcs.mx", Rol.TUTOR),
                Triple("Mtra. Laura Peña", "lpena@uabcs.mx", Rol.COORDINACION),
            ).filterNot { usuarios.existsByCorreo(it.second) }
                .forEach { (n, c, r) -> usuarios.save(Usuario(nombre = n, correo = c, passwordHash = hash, rol = r)) }
        }
}