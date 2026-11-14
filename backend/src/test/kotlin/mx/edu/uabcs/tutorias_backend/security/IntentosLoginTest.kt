package mx.edu.uabcs.tutorias_backend.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class IntentosLoginTest {

    private var t = Instant.parse("2026-01-01T00:00:00Z")
    private val intentos = IntentosLogin().also { it.ahora = { t } }
    private val correo = "a@uabcs.mx"

    @Test
    fun `bloquea tras 5 fallos y libera a los 15 minutos`() {
        repeat(4) { intentos.fallo(correo) }
        assertFalse(intentos.bloqueado(correo))

        intentos.fallo(correo)
        assertTrue(intentos.bloqueado(correo))

        t = t.plus(Duration.ofMinutes(16))
        assertFalse(intentos.bloqueado(correo))
    }

    @Test
    fun `un login correcto reinicia el conteo`() {
        repeat(4) { intentos.fallo(correo) }
        intentos.exito(correo)
        intentos.fallo(correo)
        assertFalse(intentos.bloqueado(correo))
    }
}