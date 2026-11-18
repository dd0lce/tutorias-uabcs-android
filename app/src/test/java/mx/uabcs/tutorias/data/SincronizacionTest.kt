package mx.uabcs.tutorias.data

import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SincronizacionTest {
    private val ahora = LocalDateTime.of(2026, 10, 12, 8, 0)

    @Test
    fun cadaCambioSaleConSuRutaParaElServidor() {
        val r = TutoriasRepo { ahora }
        val cambios = mutableListOf<Cambio>()
        r.alCambiar = { cambios += it }
        val pendiente = r.solicitudes.first { it.estado == Estado.PENDIENTE }

        r.rechazar(pendiente.id, "  sin cupo ")
        r.agregarMateria("sofia", "Econometría")
        r.marcarLeido("x:NUEVA")
        r.marcarLeido("x:NUEVA") // ya leído: no se vuelve a mandar

        assertEquals(
            listOf(
                Cambio("solicitudes/${pendiente.id}/rechazar", mapOf("motivo" to "sin cupo")),
                Cambio("tutores/sofia/agregar-materia", mapOf("materia" to "Econometría")),
                Cambio("avisos/leidos", mapOf("ids" to listOf("x:NUEVA"))),
            ),
            cambios,
        )
    }

    @Test
    fun unCambioRechazadoLocalmenteNoSeManda() {
        val r = TutoriasRepo { ahora }
        val cambios = mutableListOf<Cambio>()
        r.alCambiar = { cambios += it }
        runCatching { r.quitarMateria("ruben", "No existe") }
        runCatching { r.aceptar(r.solicitudes.first { it.estado == Estado.CONFIRMADA }.id) }
        assertEquals(listOf("tutores/ruben/quitar-materia"), cambios.map { it.ruta }) // quitar sí pasa: tiene 3 materias
    }

    @Test
    fun laSolicitudNuevaViajaComoJsonYVuelve() {
        val r = TutoriasRepo { ahora }
        var enviado: Cambio? = null
        r.alCambiar = { enviado = it }
        val t = r.tutor("mariana")
        val nueva = r.enviar(
            Solicitud(
                "", t.id, TipoSesion.EXAMEN, Modalidad.EN_LINEA, Formato.GRUPAL, listOf("ana@alu.uabcs.mx"),
                t.materias.first(), "Derivadas", "Repasar reglas de derivación.", null, r.horariosLibres(t).first(), Estado.PENDIENTE,
                estudiante = Estudiante("yo@alu.uabcs.mx", "Yo Mero", "Derecho · 1.er semestre"),
            ),
        )
        val cuerpo = json(enviado!!.datos) as JSONObject
        assertEquals("solicitudes", enviado!!.ruta)
        assertEquals("GRUPAL", cuerpo.getString("formato"))
        assertEquals(ahora, nueva.creadaEn)
        assertTrue("el estudiante lo pone el servidor", !cuerpo.has("estudiante"))

        // El servidor contesta con la misma forma más el estado.
        cuerpo.put("estado", "CONFIRMADA").put("nota", JSONObject.NULL).put("coordinacionPuedeVer", false)
            .put("asistio", JSONObject.NULL).put("notasSesion", JSONObject.NULL).put("creadaEn", "2026-10-12T08:00:00")
            .put("estudiante", JSONObject().put("correo", "yo@alu.uabcs.mx").put("nombre", "Yo Mero").put("detalle", "Derecho · 1.er semestre"))
        assertEquals(nueva, solicitudDe(cuerpo))
    }

    @Test
    fun cargarReemplazaLoQueHabia() {
        val r = TutoriasRepo { ahora }
        val tutor = tutorDe(
            JSONObject(
                """{"id":"t1","nombre":"Ana Ruiz","titulo":"Tutora","area":"Derecho","resumen":"Derecho","materias":["Civil"],
                "calificacion":5.0,"sesiones":0,"modalidades":["EN_LINEA"],"formatos":["INDIVIDUAL"],"lugar":"Aula 1",
                "confirmaAutomatico":false,"experiencia":[],"horarios":["2026-10-14T09:00:00"]}""",
            ),
        )
        r.cargar(listOf(tutor), emptyList(), listOf("a:NUEVA"))
        assertEquals(listOf("t1"), r.tutores.map { it.id })
        assertEquals(LocalDateTime.of(2026, 10, 14, 9, 0), r.tutor("t1").horarios.single())
        assertTrue(r.solicitudes.isEmpty())
        assertEquals(listOf("a:NUEVA"), r.leidos.toList())
    }

    @Test
    fun coordinacionSinPermisoNoRecibeAlEstudiante() {
        val o = JSONObject(
            """{"id":"s1","tutorId":"mariana","tipo":"MATERIA","modalidad":"PRESENCIAL","formato":"INDIVIDUAL","companeros":[],
            "materia":"Cálculo","tema":"","objetivo":"","archivo":null,"inicio":null,"estado":"PENDIENTE","nota":null,
            "coordinacionPuedeVer":false,"asistio":null,"notasSesion":null,"estudiante":null,"creadaEn":"2026-10-12T08:00:00"}""",
        )
        assertEquals(null, solicitudDe(o).estudiante)
    }

    @Test
    fun usuarioDelLogin() {
        val u = usuarioDe(JSONObject("""{"nombre":"Mariana Cota","correo":"mcota@uabcs.mx","rol":"TUTOR","detalle":"Mate","tutorId":"mariana"}"""))
        assertEquals(Usuario("Mariana Cota", "mcota@uabcs.mx", Rol.TUTOR, "Mate", "mariana"), u)
        assertEquals(null, usuarioDe(JSONObject("""{"nombre":"A","correo":"a@alu.uabcs.mx","rol":"ESTUDIANTE","detalle":"","tutorId":null}""")).tutorId)
    }

    @Test
    fun avisosInstitucionalesSeVenHastaSuFechaYSeSincronizan() {
        var hoy = ahora
        val r = TutoriasRepo(demo = false) { hoy }
        val cambios = mutableListOf<Cambio>()
        r.alCambiar = { cambios += it }
        assertThrows(IllegalArgumentException::class.java) { r.publicarAviso("Semana", "En biblioteca", 0) }
        val a = r.publicarAviso(" Semana de Cálculo ", "En Biblioteca Central.", 3)
        assertEquals(LocalDate.of(2026, 10, 14), a.hasta)
        assertEquals(Cambio("avisos-institucionales", mapOf("id" to a.id, "titulo" to "Semana de Cálculo", "detalle" to "En Biblioteca Central.", "hasta" to a.hasta)), cambios.single())
        hoy = ahora.plusDays(3)
        assertTrue(r.avisosVigentes().isEmpty())
        r.quitarAviso(a.id)
        assertEquals(Cambio("avisos-institucionales/${a.id}/quitar"), cambios.last())
        assertEquals(a, avisoInstitucionalDe(JSONObject("""{"id":"${a.id}","titulo":"Semana de Cálculo","detalle":"En Biblioteca Central.","hasta":"2026-10-14"}""")))
    }

    @Test
    fun correoDelTutorSoloSiLoMuestra() {
        val base = """"id":"t1","nombre":"Ana Ruiz","titulo":"Tutora","area":"Derecho","resumen":"Derecho","materias":[],
            "calificacion":5.0,"sesiones":0,"modalidades":[],"formatos":[],"lugar":"","confirmaAutomatico":false,"experiencia":[],"horarios":[]"""
        assertEquals(null, tutorDe(JSONObject("{$base,\"correo\":null}")).correo)
        assertEquals("aruiz@uabcs.mx", tutorDe(JSONObject("{$base,\"correo\":\"aruiz@uabcs.mx\"}")).correo)
        assertTrue(usuarioDe(JSONObject("""{"nombre":"Ana","correo":"a@uabcs.mx","rol":"TUTOR","detalle":"","tutorId":"t1","mostrarCorreo":true}""")).mostrarCorreo)
    }
}
