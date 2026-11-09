package mx.uabcs.tutorias.data

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuentasTest {

    @Test
    fun soloAceptaCorreosInstitucionales() {
        assertTrue(Cuentas.esInstitucional("derrick.ramirez@alu.uabcs.mx"))
        assertTrue(Cuentas.esInstitucional("  MCota@UABCS.mx "))
        assertFalse(Cuentas.esInstitucional("derrick@gmail.com"))
        assertFalse(Cuentas.esInstitucional("@uabcs.mx"))
        assertFalse(Cuentas.esInstitucional("alguien@falso-uabcs.mx"))
    }

    @Test
    fun companerosSinRepetirNiElPropio() {
        val yo = "derrick.ramirez@alu.uabcs.mx"
        assertTrue(Cuentas.companerosValidos(listOf("ana@alu.uabcs.mx", "beto@alu.uabcs.mx"), yo))
        assertFalse(Cuentas.companerosValidos(emptyList(), yo))
        assertFalse(Cuentas.companerosValidos(listOf("ana@alu.uabcs.mx", " ANA@alu.uabcs.mx"), yo))
        assertFalse(Cuentas.companerosValidos(listOf("ana@alu.uabcs.mx", "Derrick.Ramirez@alu.uabcs.mx"), yo))
        assertFalse(Cuentas.companerosValidos(listOf("ana@gmail.com"), yo))
        assertFalse(Cuentas.companerosValidos((1..5).map { "a$it@alu.uabcs.mx" }, yo))
    }

    @Test
    fun cadaCuentaEntraConSuRol() {
        val c = Cuentas.CONTRASENA_DEMO
        assertEquals(Rol.ESTUDIANTE, Cuentas.iniciarSesion("derrick.ramirez@alu.uabcs.mx", c)?.rol)
        assertEquals(Rol.TUTOR, Cuentas.iniciarSesion(" MCOTA@uabcs.mx", c)?.rol)
        assertEquals(Rol.COORDINACION, Cuentas.iniciarSesion("lpena@uabcs.mx", c)?.rol)
    }

    @Test
    fun rechazaContrasenaIncorrectaOCorreoDesconocido() {
        assertNull(Cuentas.iniciarSesion("mcota@uabcs.mx", "incorrecta"))
        assertNull(Cuentas.iniciarSesion("nadie@uabcs.mx", Cuentas.CONTRASENA_DEMO))
    }

    @Test
    fun cuentaDeTutorApuntaASuTutor() {
        val tutora = Cuentas.buscar("mcota@uabcs.mx")!!
        assertEquals(tutora.nombre, TutoriasRepo { LocalDateTime.now() }.tutor(tutora.tutorId!!).nombre)
        assertNull(Cuentas.buscar("derrick.ramirez@alu.uabcs.mx")!!.tutorId)
        assertNull(Cuentas.buscar("lpena@uabcs.mx")!!.tutorId)
    }
}
