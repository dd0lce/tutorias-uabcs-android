package mx.uabcs.tutorias.data

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
}
