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
    fun tutorCancelaConMotivoSoloSesionesFuturas() {
        val r = repo()
        val futura = r.solicitudes.first { it.estado == Estado.CONFIRMADA && it.inicio!!.isAfter(ahora) }
        val pasada = r.solicitudes.first { it.estado == Estado.CONFIRMADA && it.inicio!!.isBefore(ahora) }
        assertThrows(IllegalArgumentException::class.java) { r.cancelarComoTutor(futura.id, " ") }
        assertThrows(IllegalArgumentException::class.java) { r.cancelarComoTutor(pasada.id, "Junta") }

        r.cancelarComoTutor(futura.id, " Junta de academia ")
        val cancelada = r.solicitud(futura.id)!!
        assertEquals(Estado.CANCELADA, cancelada.estado)
        assertEquals("Cancelada por el tutor. Motivo: Junta de academia", cancelada.nota)
        assertTrue(futura.inicio in r.horariosLibres(r.tutor(futura.tutorId)))
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
        assertEquals(setOf("sofia", "luis"), r.parecidos(r.tutor("mariana")).map { it.id }.toSet())
        assertEquals(setOf("mariana", "luis"), r.parecidos(r.tutor("ruben")).map { it.id }.toSet())
    }

    @Test
    fun solicitarCambioDejaPendienteYConservaElHorario() {
        val r = repo()
        val cita = r.solicitudes.first {it.estado == Estado.CONFIRMADA}
        r.solicitarCambio(cita.id, " ¿Podria ser el viernes?")
        val cambiada = r.solicitud(cita.id)!!
        assertEquals(Estado.PENDIENTE, cambiada.estado)
        assertEquals("Cambio solicitado: ¿Podria ser el viernes?", cambiada.nota)
        assertEquals(cita.inicio, cambiada.inicio)
        assertFalse(cita.inicio in r.horariosLibres(r.tutor(cita.tutorId)))
    }

    @Test
    fun listaDeEsperaNumeraPorTutor() {
        val r = repo()
        val base = r.solicitudes.first().copy(id = "", tutorId = "ruben", inicio = null, nota = null)
        assertEquals("posición 1 en la lista", r.enviar(base).nota)
        assertEquals("posición 2 en la lista", r.enviar(base).nota)
    }

    @Test
    fun registrarSesionSoloSiYaOcurrio() {
        val r = repo()
        val pasada = r.porRegistrar("mariana").single()
        assertTrue(pasada.inicio!!.isBefore(ahora))

        val futura = r.solicitudes.first { it.tutorId == "mariana" && it.estado == Estado.CONFIRMADA && it.inicio!!.isAfter(ahora) }
        assertThrows(IllegalArgumentException::class.java) { r.registrarSesion(futura.id, true, "") }

        r.registrarSesion(pasada.id, asistio = false, notas = "  No llegó; se le escribió por correo. ")
        val hecha = r.solicitud(pasada.id)!!
        assertEquals(Estado.COMPLETADA, hecha.estado)
        assertEquals(false, hecha.asistio)
        assertEquals("No llegó; se le escribió por correo.", hecha.notasSesion)
        assertTrue(r.porRegistrar("mariana").isEmpty())
        assertEquals(listOf(pasada.id), r.realizadas("mariana").map { it.id })
        assertThrows(IllegalArgumentException::class.java) { r.registrarSesion(pasada.id, true, "") }
    }

    @Test
    fun tutorAbreYCierraHorarios() {
        val r = repo()
        val nuevo = ahora.plusDays(3).withHour(7)
        assertFalse(nuevo in r.tutor("mariana").horarios)

        r.alternarHorario("mariana", nuevo)
        assertTrue(nuevo in r.horariosLibres(r.tutor("mariana")))
        r.alternarHorario("mariana", nuevo)
        assertFalse(nuevo in r.tutor("mariana").horarios)

        val agendado = r.solicitudes.first { it.tutorId == "mariana" && it.activa && it.inicio!!.isAfter(ahora) }.inicio!!
        assertThrows(IllegalArgumentException::class.java) { r.alternarHorario("mariana", agendado) }
        assertTrue(agendado in r.tutor("mariana").horarios)
        assertThrows(IllegalArgumentException::class.java) { r.alternarHorario("mariana", ahora.minusHours(1)) }
    }

    @Test
    fun avisosPorRolYLeidos() {
        val r = repo()
        val estudiante = Cuentas.buscar("derrick.ramirez@alu.uabcs.mx")!!
        val pendiente = r.solicitudes.first { it.estado == Estado.PENDIENTE }
        val tutor = Usuario("Tutor", "t@uabcs.mx", Rol.TUTOR, "", tutorId = pendiente.tutorId)

        assertTrue(r.avisos(tutor).any { it.tipo == TipoAviso.NUEVA && it.solicitud.id == pendiente.id })
        assertEquals(TipoAviso.POR_REGISTRAR, r.avisos(Cuentas.buscar("mcota@uabcs.mx")!!).single().tipo)
        assertTrue(r.avisos(estudiante).none { it.solicitud.inicio?.isBefore(ahora) == true })

        r.marcarLeidos(estudiante)
        assertEquals(0, r.sinLeer(estudiante))
        r.rechazar(pendiente.id, "Tengo junta")
        assertEquals(1, r.sinLeer(estudiante))
        assertTrue(r.avisos(tutor).none { it.solicitud.id == pendiente.id })
        assertTrue(r.avisos(Cuentas.buscar("lpena@uabcs.mx")!!).isEmpty())
    }
}
