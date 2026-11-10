package mx.edu.uabcs.tutorias_backend.repository

import mx.edu.uabcs.tutorias_backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UsuarioRepository : JpaRepository<Usuario, Long> {

    fun findByCorreo(correo: String): Optional<Usuario>

    fun existsByCorreo(correo: String): Boolean
}