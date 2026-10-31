package mx.uabcs.tutorias.data

import java.time.DayOfWeek
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TutoriasTest {
    private val ahora = LocalDateTime.of(2026, 10, 12, 8, 0)
    private fun repo() = TutoriasRepo { ahora }

    private fun TutoriasRepo.ids(f: Filtros) = buscar(f).map { it.id }.toSet()

    @Test
    fun buscarIgnoraAcentosYMayusculas() {
        assertEquals(setOf("mariana", "luis", "ruben"), repo().ids(Filtros(texto = "CALCULO")))
    }

    @Test
    fun filtraPorModalidadDiaYFranja() {
        val r = repo()
        assertEquals(setOf("mariana", "luis"), r.ids(Filtros(modalidades = setOf(Modalidad.EN_LINEA))))
        assertEquals(setOf("sofia"), r.ids(Filtros(dias = setOf(DayOfWeek.SATURDAY), franjas = setOf(Franja.MANANA))))
    }

    @Test
    fun respetaAnticipacionDe24Horas() {
        val r = repo()
        r.tutores.flatMap(r::horariosLibres).forEach { assertTrue(it.isAfter(ahora.plusHours(24))) }
        assertFalse(LocalDateTime.of(2026, 10, 12, 17, 0) in r.horariosLibres(r.tutor("mariana")))
    }

    @Test
    fun enviarConfirmaPendienteOListaDeEspera() {
        val r = repo()
        val mariana = r.tutor("mariana")
        val h = r.horariosLibres(mariana).first()
        val base = r.solicitudes.first().copy(id = "", nota = null)

        assertEquals(Estado.CONFIRMADA, r.enviar(base.copy(tutorId = "mariana", inicio = h)).estado)
        assertFalse(h in r.horariosLibres(mariana))

        val hs = r.horariosLibres(r.tutor("sofia")).first()
        assertEquals(Estado.PENDIENTE, r.enviar(base.copy(tutorId = "sofia", inicio = hs)).estado)
        assertEquals(Estado.LISTA_ESPERA, r.enviar(base.copy(tutorId = "ruben", inicio = null)).estado)
    }

    @Test
    fun noPermiteHorarioOcupado() {
        val r = repo()
        val ocupado = r.solicitudes.first { it.estado == Estado.CONFIRMADA }
        assertThrows(IllegalArgumentException::class.java) { r.enviar(ocupado.copy(id = "")) }
    }

    @Test
    fun cancelarLiberaElHorario() {
        val r = repo()
        val cita = r.solicitudes.first { it.estado == Estado.CONFIRMADA }
        r.cancelar(cita.id)
        assertEquals(Estado.CANCELADA, r.solicitud(cita.id)?.estado)
        assertTrue(cita.inicio in r.horariosLibres(r.tutor(cita.tutorId)))
    }

    @Test
    fun cambiarHorarioConfirmaSoloSiElTutorLoPermite() {
        val r = repo()
        val conMariana = r.solicitudes.first { it.tutorId == "mariana" && it.activa }
        r.cambiarHorario(conMariana.id, r.horariosLibres(r.tutor("mariana")).first())
        assertEquals(Estado.CONFIRMADA, r.solicitud(conMariana.id)?.estado)
        assertTrue(conMariana.inicio in r.horariosLibres(r.tutor("mariana")))

        val conLuis = r.solicitudes.first { it.tutorId == "luis" && it.activa }
        r.cambiarHorario(conLuis.id, r.horariosLibres(r.tutor("luis")).first())
        assertEquals(Estado.PENDIENTE, r.solicitud(conLuis.id)?.estado)
    }

    @Test
    fun aceptarConfirmaSoloPendientes() {
        val r = repo()
        val pendiente = r.solicitudes.first { it.estado == Estado.PENDIENTE }
        r.aceptar(pendiente.id)
        assertEquals(Estado.CONFIRMADA, r.solicitud(pendiente.id)?.estado)
        assertThrows(IllegalArgumentException::class.java) { r.aceptar(pendiente.id) }
    }

    @Test
    fun rechazarPideMotivoYLiberaElHorario() {
        val r = repo()
        val pendiente = r.solicitudes.first { it.estado == Estado.PENDIENTE }
        assertThrows(IllegalArgumentException::class.java) { r.rechazar(pendiente.id, "  ") }
        assertEquals(Estado.PENDIENTE, r.solicitud(pendiente.id)?.estado)

        r.rechazar(pendiente.id, " Tengo junta de academia ")
        val rechazada = r.solicitud(pendiente.id)!!
        assertEquals(Estado.RECHAZADA, rechazada.estado)
        assertEquals("Motivo: Tengo junta de academia", rechazada.nota)
        assertTrue(pendiente.inicio in r.horariosLibres(r.tutor(pendiente.tutorId)))
    }

    @Test
    fun inicialesIgnoranEspaciosDobles() {
        val ruben = repo().tutor("ruben")
        assertEquals("RC", ruben.iniciales)
        assertEquals("RC", ruben.copy(nombre = "Rubén  Castro  Higuera").iniciales)
    }

    @Test
    fun parecidosCompartenAreaYTienenHorarioLibre() {
        val r = repo()
        assertEquals(set0f("sofia", "luis"), r.parecidos(r.tutor("mariana")).map {it.id}.toSet())
        assertEquals(set0f("mariana", "luis"), r.parecidos(r.tutor("ruben")).map {it.id}.toSet())
    }

    @Test
    fun solicitarCambioDejaPendienteYConservaElHorario() {
        val r = repo()
        val cita = r.solicitudes.first {it.estado == Estado.CONFIRMADA}
        r.solicitarCambio(cita.id, " ¿Podria ser el viernes?")
        val cambiada = r,solicitud(cita.id)!!
        assertEquals(Estado.PENDIENTE, cambiada.estado)
        assertEquals("Cambio solicitado: ¿Podria ser el viernes?", cambiada.nota)
        assertEquals(cita.inicio, cambiada.inicio)
        assertEquals(cita.incio in r.horariosLibres(r.tutor(cita.tutorId)))
    }

    @Test
    fun listaDeEsperaNumeraPorTutor() {
        val r = repo()
        val base = r.solicittudes.firts().copy(id = "", tutorId = "ruben", inicio = null, nota = null)
        assertEquals("posicion 1 en la lista de espera", r.enviar(base).nota)
        assertEquals("posicion 2 en la lista de espera", r.enviar(base).nota)
    }
}
