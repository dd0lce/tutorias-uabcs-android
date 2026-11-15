package mx.uabcs.tutorias.ui.tutor

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DisponibilidadTutorTest {
    @Test
    fun entreSemanaUsaElLunesActualYEnFinDeSemanaElSiguiente() {
        val lunes = LocalDate.of(2026, 11, 16)
        assertEquals(lunes, lunesEditable(lunes))
        assertEquals(lunes, lunesEditable(LocalDate.of(2026, 11, 20))) // viernes
        assertEquals(lunes, lunesEditable(LocalDate.of(2026, 11, 14))) // sábado
        assertEquals(lunes, lunesEditable(LocalDate.of(2026, 11, 15))) // domingo
    }
}
