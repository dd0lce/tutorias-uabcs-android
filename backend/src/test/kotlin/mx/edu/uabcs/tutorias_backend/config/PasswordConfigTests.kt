package mx.edu.uabcs.tutorias_backend.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PasswordConfigTest {

    private val encoder = PasswordConfig().passwordEncoder()

    @Test
    fun `la contraseña se convierte en hash` () {
        val password = "mi_contraseña_segura"
        val hash = encoder.encode(password)

        assertNotEquals(password, hash)
        assertTrue(encoder.matches(password, hash))

    }

    @Test
    fun `dos hashes de la misma contraseña son distintos`() {
        assertNotEquals(encoder.encode("x"), encoder.encode("x"))
    }
}