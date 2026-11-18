package mx.uabcs.tutorias.ui.tutor

import org.junit.Assert.assertEquals
import org.junit.Test

class SolicitudesTutorTest {
    @Test
    fun inicialesEstudianteUsaLasPrimerasDosPalabrasYNormalizaEspacios() {
        assertEquals("AS", inicialesEstudiante("  ana   sofía Cota "))
    }

    @Test
    fun inicialesEstudianteAceptaNombreDeUnaSolaPalabra() {
        assertEquals("L", inicialesEstudiante("Luis"))
    }

    @Test
    fun inicialesEstudianteDevuelveVacioParaNombreEnBlanco() {
        assertEquals("", inicialesEstudiante("  "))
    }
}
