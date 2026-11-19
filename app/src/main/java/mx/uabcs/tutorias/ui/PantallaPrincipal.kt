package mx.uabcs.tutorias.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import mx.uabcs.tutorias.ui.notificaciones.Flotantes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mx.uabcs.tutorias.R
import mx.uabcs.tutorias.data.Api
import mx.uabcs.tutorias.data.Cambio
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.TipoAviso
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.coordinacion.ContenidoAcademico
import mx.uabcs.tutorias.ui.coordinacion.PanelCoordinacion
import mx.uabcs.tutorias.ui.perfil.EditarPerfil
import mx.uabcs.tutorias.ui.perfil.Perfil
import mx.uabcs.tutorias.ui.coordinacion.SesionesAutorizadasCoordinacion
import mx.uabcs.tutorias.ui.coordinacion.TutoresCoordinacion
import mx.uabcs.tutorias.ui.estudiante.BuscarTutor
import mx.uabcs.tutorias.ui.estudiante.CambiarHorario
import mx.uabcs.tutorias.ui.estudiante.DetalleCita
import mx.uabcs.tutorias.ui.estudiante.InicioEstudiante
import mx.uabcs.tutorias.ui.estudiante.MisSolicitudes
import mx.uabcs.tutorias.ui.estudiante.NuevaSolicitud
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.estudiante.PerfilTutor
import mx.uabcs.tutorias.ui.estudiante.SolicitudEnviada
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.notificaciones.Notificaciones
import mx.uabcs.tutorias.ui.tutor.DetalleSesion
import mx.uabcs.tutorias.ui.tutor.DisponibilidadTutor
import mx.uabcs.tutorias.ui.tutor.InicioTutor
import mx.uabcs.tutorias.ui.tutor.RegistrarSesion
import mx.uabcs.tutorias.ui.tutor.SesionesTutor
import mx.uabcs.tutorias.ui.tutor.SolicitudesTutor

data class Destino(val etiqueta: String, val icono: ImageVector)

private val perfil = Destino("Perfil", Iconos.perfil)
private val mensajes = Destino("Mensajes", Iconos.mensajes)
private val inicio = Destino("Inicio", Iconos.inicio)
private val buscar = Destino("Buscar", Iconos.buscar)
private val misSolicitudes = Destino("Mis solicitudes", Iconos.solicitudes)
private val solicitudesTutor = Destino("Solicitudes", Iconos.bandeja)
private val sesionesTutor = Destino("Sesiones", Iconos.historial)

private val panelCoordinacion = Destino("Panel", Iconos.panel)
private val sesionesAutorizadasCoordinacion = Destino("Sesiones autorizadas", Iconos.escudo)
private val tutoresCoordinacion = Destino("Tutores", Iconos.grupo)

fun destinosDe(rol: Rol): List<Destino> = when (rol) {
    Rol.ESTUDIANTE -> listOf(inicio, buscar, misSolicitudes, mensajes, perfil)
    Rol.TUTOR -> listOf(inicio, solicitudesTutor, sesionesTutor, mensajes, perfil)
    Rol.COORDINACION -> listOf(
        panelCoordinacion,
        sesionesAutorizadasCoordinacion,
        tutoresCoordinacion,
        perfil,
    )
}

enum class TipoRuta(val titulo: String) {
    PERFIL_TUTOR("Perfil del tutor"),
    NUEVA_SOLICITUD("Nueva solicitud"),
    ENVIADA("Solicitud enviada"),
    DETALLE("Detalle de la cita"),
    CAMBIAR("Cambiar horario"),
    AYUDA("Ayuda"),
    NOTIFICACIONES("Notificaciones"),
    DISPONIBILIDAD("Disponibilidad"),
    DETALLE_SESION("Detalle de la sesión"),
    REGISTRAR_SESION("Registrar sesión"),
    MENSAJES("Mensajes"),
    CONTENIDO_ACADEMICO("Contenido académico"),
    EDITAR_PERFIL("Editar perfil"),
}

data class Ruta(val tipo: TipoRuta, val id: String = "", val extra: String = "")

private val GuardarPila = listSaver<SnapshotStateList<Ruta>, String>(
    save = { pila -> pila.map { "${it.tipo.name}|${it.id}|${it.extra}" } },
    restore = { guardado ->
        mutableStateListOf<Ruta>().apply {
            guardado.forEach { s -> s.split('|').let { (t, id, extra) -> add(Ruta(TipoRuta.valueOf(t), id, extra)) } }
        }
    },
)

@Composable
fun PantallaPrincipal(usuario: Usuario, onCerrarSesion: () -> Unit) {
    val destinos = destinosDe(usuario.rol)
    var actual by rememberSaveable(usuario.correo) { mutableIntStateOf(0) }
    val pila = rememberSaveable(usuario.correo, saver = GuardarPila) { mutableStateListOf() }
    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var verCalendario by rememberSaveable { mutableStateOf(false) }
    val estados = rememberSaveableStateHolder()
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val avisar: (String) -> Unit = { msg -> alcance.launch { avisos.showSnackbar(msg) } }

    LaunchedEffect(usuario.correo) {
        val cambios = Channel<Cambio>(Channel.UNLIMITED)
        Tutorias.alCambiar = { cambios.trySend(it) }
        try {
            Api.sincronizar(cambios, avisar)
        } finally {
            Tutorias.alCambiar = null
        }
    }

    val contexto = LocalContext.current
    var permisoRespondido by remember { mutableIntStateOf(0) }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permisoRespondido++ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    // Cada recarga del servidor (cada 30 s) cambia los avisos; los nuevos sin leer salen como notificación flotante.
    LaunchedEffect(usuario.correo, permisoRespondido) {
        snapshotFlow { Tutorias.avisos(usuario).filter { it.id !in Tutorias.leidos } }
            .collect { Flotantes.mostrar(contexto, usuario.correo, it) }
    }

    fun irA(destino: Destino) {
        pila.clear()
        actual = destinos.indexOf(destino)
    }

    BackHandler(enabled = pila.isNotEmpty() || actual != 0) {
        if (pila.isNotEmpty()) pila.removeAt(pila.lastIndex) else actual = 0
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            destinos.forEachIndexed { i, d ->
                item(
                    selected = i == actual,
                    onClick = { pila.clear(); verCalendario = false; actual = i },
                    icon = { Icon(d.icono, contentDescription = null) },
                    label = { Text(d.etiqueta, maxLines = 2, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis) },
                )
            }
        },
    ) {
        val ruta = pila.lastOrNull()
        Scaffold(
            topBar = {
                Encabezado(
                    titulo = ruta?.tipo?.titulo ?: destinos[actual].etiqueta,
                    onAtras = if (ruta != null) ({ pila.removeAt(pila.lastIndex) }) else null,
                    onAyuda = { if (ruta?.tipo != TipoRuta.AYUDA) pila.add(Ruta(TipoRuta.AYUDA)) },
                    sinLeer = Tutorias.sinLeer(usuario),
                    onNotificaciones = { if (ruta?.tipo != TipoRuta.NOTIFICACIONES) pila.add(Ruta(TipoRuta.NOTIFICACIONES)) },
                )
            },
            bottomBar = { PieLegal() },
            snackbarHost = { SnackbarHost(avisos) },
        ) { relleno ->
            val m = Modifier.padding(relleno)
            val clave = ruta?.let { "${pila.size}:${it.tipo}:${it.id}" } ?: "seccion:$actual"
            val verContenido: (String) -> Unit = { id -> pila.add(Ruta(TipoRuta.CONTENIDO_ACADEMICO, id)) }
            val abrirChat: (String) -> Unit = { id -> pila.add(Ruta(TipoRuta.MENSAJES, id)) }
            estados.SaveableStateProvider(clave) {
                when {
                    // Sin tutores todavía no llegan los datos del servidor; las pantallas los necesitan.
                    Tutorias.tutores.isEmpty() -> Cargando(onCerrarSesion, m)
                    ruta != null -> when (ruta.tipo) {
                        TipoRuta.EDITAR_PERFIL -> EditarPerfil(usuario, onListo = { pila.removeAt(pila.lastIndex) }, avisar, m)
                        TipoRuta.PERFIL_TUTOR -> PerfilTutor(
                            ruta.id,
                            onSolicitar = if (usuario.rol == Rol.ESTUDIANTE) {
                                { h -> pila.add(Ruta(TipoRuta.NUEVA_SOLICITUD, ruta.id, h?.toString().orEmpty())) }
                            } else null,
                            modifier = m,
                        )
                        TipoRuta.NUEVA_SOLICITUD -> NuevaSolicitud(
                            ruta.id,
                            estudiante = usuario.comoEstudiante(),
                            horarioInicial =ruta.extra.takeIf { it.isNotEmpty() }?.let(LocalDateTime::parse),
                            onEnviada = { id -> pila.clear(); pila.add(Ruta(TipoRuta.ENVIADA, id)) },
                            onCancelar = { pila.removeAt(pila.lastIndex) },
                            onOtroTutor = { id -> pila[pila.lastIndex] = Ruta(TipoRuta.PERFIL_TUTOR, id) },
                            avisar = avisar,
                            modifier = m,
                        )
                        TipoRuta.ENVIADA -> SolicitudEnviada(
                            ruta.id,
                            onMisSolicitudes = { verCalendario = false; irA(misSolicitudes) },
                            onInicio = { irA(inicio) },
                            modifier = m,
                        )
                        TipoRuta.DETALLE -> DetalleCita(
                            ruta.id,
                            onCambiar = { pila.add(Ruta(TipoRuta.CAMBIAR, ruta.id)) },
                            onMensajes = { pila.add(Ruta(TipoRuta.MENSAJES, ruta.id)) },
                            onCancelada = { irA(misSolicitudes); avisar("Cita cancelada. Le avisamos al tutor.") },
                            avisar = avisar,
                            modifier = m,
                        )
                        TipoRuta.CAMBIAR -> CambiarHorario(
                            ruta.id,
                            onListo = { msg -> pila.removeAt(pila.lastIndex); avisar(msg) },
                            avisar = avisar,
                            modifier = m,
                        )
                        TipoRuta.AYUDA -> Ayuda(usuario.rol, modifier = m)
                        TipoRuta.NOTIFICACIONES -> Notificaciones(
                            usuario,
                            onAbrir = { a ->
                                when {
                                    // Las pendientes se aceptan o rechazan desde Solicitudes, que abre en la pestaña Nuevas.
                                    a.tipo == TipoAviso.NUEVA || a.tipo == TipoAviso.CAMBIO -> {
                                        estados.removeState("seccion:${destinos.indexOf(solicitudesTutor)}")
                                        irA(solicitudesTutor)
                                    }
                                    usuario.rol == Rol.TUTOR -> pila.add(Ruta(TipoRuta.DETALLE_SESION, a.solicitud.id))
                                    else -> pila.add(Ruta(TipoRuta.DETALLE, a.solicitud.id))
                                }
                            },
                            modifier = m,
                        )
                        TipoRuta.DISPONIBILIDAD -> DisponibilidadTutor(checkNotNull(usuario.tutorId), avisar = avisar, modifier = m)
                        TipoRuta.DETALLE_SESION -> DetalleSesion(
                            ruta.id,
                            onRegistrar = { pila.add(Ruta(TipoRuta.REGISTRAR_SESION, ruta.id)) },
                            onMensajes = { pila.add(Ruta(TipoRuta.MENSAJES, ruta.id)) },
                            onCancelada = { pila.removeAt(pila.lastIndex); avisar("Sesión cancelada. El horario quedó libre.") },
                            modifier = m,
                        )
                        TipoRuta.REGISTRAR_SESION -> RegistrarSesion(
                            ruta.id,
                            onListo = { pila.removeAt(pila.lastIndex); avisar("Registro guardado.") },
                            modifier = m,
                        )
                        TipoRuta.MENSAJES -> Chat(ruta.id, avisar = avisar, modifier = m)
                        TipoRuta.CONTENIDO_ACADEMICO -> ContenidoAcademico(ruta.id, modifier = m)
                    }
                    destinos[actual] == perfil -> Perfil(
                        usuario,
                        onEditar = { pila.add(Ruta(TipoRuta.EDITAR_PERFIL)) },
                        onHorario = if (usuario.rol == Rol.TUTOR) ({ pila.add(Ruta(TipoRuta.DISPONIBILIDAD)) }) else null,
                        onCerrarSesion = onCerrarSesion,
                        avisar = avisar,
                        modifier = m,
                    )
                    usuario.rol == Rol.ESTUDIANTE -> when (destinos[actual]) {
                        inicio -> InicioEstudiante(
                            usuario,
                            onBuscar = { texto -> textoBusqueda = texto; irA(buscar) },
                            onVerSolicitudes = { calendario -> verCalendario = calendario; irA(misSolicitudes) },
                            onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE, id)) },
                            onMensajes = abrirChat,
                            modifier = m,
                        )
                        buscar -> BuscarTutor(textoBusqueda, onTutor = { id -> pila.add(Ruta(TipoRuta.PERFIL_TUTOR, id)) }, modifier = m)
                        misSolicitudes -> MisSolicitudes(
                            verCalendario,
                            onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE, id)) },
                            onMensajes = abrirChat,
                            modifier = m,
                        )
                        mensajes -> Conversaciones(usuario, onAbrir = abrirChat, modifier = m)
                        else -> {}
                    }
                    usuario.rol == Rol.TUTOR -> {
                        val tutorId = checkNotNull(usuario.tutorId) { "La cuenta de tutor ${usuario.correo} no tiene tutorId" }
                        when (destinos[actual]) {
                            inicio -> InicioTutor(
                                tutorId,
                                onSolicitudes = { irA(solicitudesTutor) },
                                onSesiones = { irA(sesionesTutor) },
                                onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE_SESION, id)) },
                                onHorario = { pila.add(Ruta(TipoRuta.DISPONIBILIDAD)) },
                                modifier = m,
                            )
                            solicitudesTutor -> SolicitudesTutor(
                                tutorId,
                                onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE_SESION, id)) },
                                avisar = avisar,
                                modifier = m,
                            )
                            sesionesTutor -> SesionesTutor(
                                tutorId,
                                onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE_SESION, id)) },
                                modifier = m,
                            )
                            mensajes -> Conversaciones(usuario, onAbrir = abrirChat, modifier = m)
                            else -> {}
                        }
                    }
                    usuario.rol == Rol.COORDINACION -> when (destinos[actual]) {
                        panelCoordinacion -> PanelCoordinacion(verContenido, modifier = m)
                        sesionesAutorizadasCoordinacion -> SesionesAutorizadasCoordinacion(verContenido, modifier = m)
                        tutoresCoordinacion -> TutoresCoordinacion(
                            onTutor = { id -> pila.add(Ruta(TipoRuta.PERFIL_TUTOR, id)) },
                            modifier = m,
                        )
                        else -> {}
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Encabezado(titulo: String, onAtras: (() -> Unit)?, onAyuda: () -> Unit, sinLeer: Int, onNotificaciones: () -> Unit) {
    Column {
        TopAppBar(
            title = { Text(titulo) },
            navigationIcon = {
                if (onAtras != null) {
                    IconButton(onClick = onAtras) {
                        Icon(Iconos.atras, contentDescription = "Regresar")
                    }
                } else {
                    Image(
                        painterResource(R.drawable.logo_uabcs),
                        contentDescription = "Logotipo de la UABCS",
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp).size(40.dp),
                    )
                }
            },
            actions = {
                IconButton(onClick = onAyuda) {
                    Icon(Iconos.ayuda, contentDescription = "Ayuda")
                }
                IconButton(onClick = onNotificaciones) {
                    BadgedBox(badge = { if (sinLeer > 0) Badge { Text("$sinLeer") } }) {
                        Icon(Iconos.campana, contentDescription = "Notificaciones, $sinLeer sin leer")
                    }
                }
            },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun Cargando(onCerrarSesion: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text("Conectando con el servidor…", style = MaterialTheme.typography.bodyLarge)
        BotonSecundario(onClick = onCerrarSesion) { Text("Cerrar sesión") }
    }
}
