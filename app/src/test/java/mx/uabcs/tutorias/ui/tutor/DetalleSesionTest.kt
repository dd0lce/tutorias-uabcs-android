package mx.uabcs.tutorias.ui.tutor

import java.time.LocalDateTime
import mx.uabcs.tutorias.data.Estudiante
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.TipoSesion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetalleSesionTest {
    private fun solicitud(estudiante: Estudiante?) = Solicitud(
        id = "solicitud",
        tutorId = "tutor",
        tipo = TipoSesion.MATERIA,
        modalidad = Modalidad.PRESENCIAL,
        formato = Formato.INDIVIDUAL,
        companeros = emptyList(),
        materia = "Cálculo",
        tema = "Derivadas",
        objetivo = "Repasar ejercicios",
        archivo = null,
        inicio = LocalDateTime.of(2026, 11, 18, 10, 0),
        estado = Estado.CONFIRMADA,
        estudiante = estudiante,
    )

    @Test
    fun etiquetaEstudianteIncluyeNombreCarreraYSemestre() {
        val solicitud = solicitud(Estudiante("ana@alu.uabcs.mx", "Ana López", "Ing. en Software · 5.º semestre"))

        assertEquals("Ana López · Ing. en Software · 5.º semestre", solicitud.etiquetaEstudiante())
    }

    @Test
    fun etiquetaEstudianteEsNullSiNoHayEstudiante() {
        assertNull(solicitud(null).etiquetaEstudiante())
    }

    @Test
    fun etiquetaEstudianteOmiteDetalleVacio() {
        val solicitud = solicitud(Estudiante("ana@alu.uabcs.mx", "Ana López", " "))

        assertEquals("Ana López", solicitud.etiquetaEstudiante())
    }
}
