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

    @Test
    fun nombreArchivoSeguroEliminaRutasYConservaElNombre() {
        assertEquals("material.pdf", nombreArchivoSeguro("../privado/material.pdf"))
        assertEquals("foto.png", nombreArchivoSeguro("C:\\archivos\\foto.png"))
    }

    @Test
    fun nombreArchivoSeguroUsaNombrePorDefectoSiEstaVacio() {
        assertEquals("adjunto", nombreArchivoSeguro("  "))
        assertEquals("adjunto", nombreArchivoSeguro(null))
    }
}
