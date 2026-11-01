package mx.uabcs.tutorias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import kotlinx.coroutines.launch
import mx.uabcs.tutorias.R
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.estudiante.BuscarTutor
import mx.uabcs.tutorias.ui.estudiante.CambiarHorario
import mx.uabcs.tutorias.ui.estudiante.DetalleCita
import mx.uabcs.tutorias.ui.estudiante.InicioEstudiante
import mx.uabcs.tutorias.ui.estudiante.MisSolicitudes
import mx.uabcs.tutorias.ui.estudiante.NuevaSolicitud
import mx.uabcs.tutorias.ui.estudiante.PerfilTutor
import mx.uabcs.tutorias.ui.estudiante.SolicitudEnviada
import mx.uabcs.tutorias.ui.tutor.SolicitudesTutor

data class Destino(val etiqueta: String, val icono: ImageVector, val cubre: String)

private val perfil = Destino("Perfil", Icons.Outlined.Person, "RF005 Perfil del usuario · RF006 Datos personales · RF007 Aviso de privacidad")
private val mensajes = Destino("Mensajes", Icons.AutoMirrored.Outlined.Chat, "RF022 Mensajes por cita")
private val inicio = Destino("Inicio", Icons.Outlined.Home, "RF012 Pantalla de inicio")
private val buscar = Destino("Buscar", Icons.Outlined.Search, "RF009 Búsqueda con filtros · RF010 Perfil del tutor · RF011 Horarios")
private val misSolicitudes = Destino("Mis solicitudes", Icons.AutoMirrored.Outlined.Assignment, "RF013–RF019 Solicitudes, cambios y cancelaciones")
private val solicitudesTutor = Destino("Solicitudes", Icons.Outlined.Inbox, "RF028 Gestión de solicitudes")

fun destinosDe(rol: Rol): List<Destino> = when (rol) {
    Rol.ESTUDIANTE -> listOf(inicio, buscar, misSolicitudes, mensajes, perfil)
    Rol.TUTOR -> listOf(
        solicitudesTutor,
        Destino("Disponibilidad", Icons.Outlined.EventAvailable, "RF027 Gestión de disponibilidad"),
        Destino("Sesiones", Icons.Outlined.History, "RF029 Registro de sesión"),
        mensajes,
        perfil,
    )
    Rol.COORDINACION -> listOf(
        Destino("Panel", Icons.Outlined.Dashboard, "RNF013 Resumen del panel"),
        Destino("Sesiones autorizadas", Icons.Outlined.VerifiedUser, "RF008 Acceso de coordinación"),
        Destino("Tutores", Icons.Outlined.Groups, "RNF013 Directorio de tutores"),
        perfil,
    )
}

enum class TipoRuta(val titulo: String) {
    PERFIL_TUTOR("Perfil del tutor"),
    NUEVA_SOLICITUD("Nueva solicitud"),
    ENVIADA("Solicitud enviada"),
    DETALLE("Detalle de la cita"),
    CAMBIAR("Cambiar horario"),
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
    val pendiente = rememberPendiente()
    val avisar: (String) -> Unit = { msg -> alcance.launch { avisos.showSnackbar(msg) } }

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
                )
            },
            bottomBar = { PieLegal() },
            snackbarHost = { SnackbarHost(avisos) },
        ) { relleno ->
            val m = Modifier.padding(relleno)
            val clave = ruta?.let { "${pila.size}:${it.tipo}:${it.id}" } ?: "seccion:$actual"
            estados.SaveableStateProvider(clave) {
                when {
                    ruta != null -> when (ruta.tipo) {
                        TipoRuta.PERFIL_TUTOR -> PerfilTutor(
                            ruta.id,
                            onSolicitar = { h -> pila.add(Ruta(TipoRuta.NUEVA_SOLICITUD, ruta.id, h?.toString().orEmpty())) },
                            modifier = m,
                        )
                        TipoRuta.NUEVA_SOLICITUD -> NuevaSolicitud(
                            ruta.id,
                            horarioInicial = ruta.extra.takeIf { it.isNotEmpty() }?.let(LocalDateTime::parse),
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
                            onMensajes = pendiente,
                            onCancelada = { irA(misSolicitudes); avisar("Cita cancelada. Le avisamos al tutor.") },
                            modifier = m,
                        )
                        TipoRuta.CAMBIAR -> CambiarHorario(
                            ruta.id,
                            onListo = { msg -> pila.removeAt(pila.lastIndex); avisar(msg) },
                            modifier = m,
                        )
                    }
                    usuario.rol == Rol.ESTUDIANTE -> when (destinos[actual]) {
                        inicio -> InicioEstudiante(
                            usuario,
                            onBuscar = { texto -> textoBusqueda = texto; irA(buscar) },
                            onVerSolicitudes = { calendario -> verCalendario = calendario; irA(misSolicitudes) },
                            onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE, id)) },
                            onMensajes = pendiente,
                            modifier = m,
                        )
                        buscar -> BuscarTutor(textoBusqueda, onTutor = { id -> pila.add(Ruta(TipoRuta.PERFIL_TUTOR, id)) }, modifier = m)
                        misSolicitudes -> MisSolicitudes(
                            verCalendario,
                            onDetalle = { id -> pila.add(Ruta(TipoRuta.DETALLE, id)) },
                            onMensajes = pendiente,
                            modifier = m,
                        )
                        else -> PantallaPendiente(destinos[actual], usuario, onCerrarSesion, m)
                    }
                    usuario.rol == Rol.TUTOR -> when (destinos[actual]) {
                        solicitudesTutor -> SolicitudesTutor(
                            checkNotNull(usuario.tutorId) { "La cuenta de tutor ${usuario.correo} no tiene tutorId" },
                            avisar = avisar,
                            modifier = m,
                        )
                        else -> PantallaPendiente(destinos[actual], usuario, onCerrarSesion, m)
                    }
                    else -> PantallaPendiente(destinos[actual], usuario, onCerrarSesion, m)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Encabezado(titulo: String, onAtras: (() -> Unit)?) {
    val pendiente = rememberPendiente()
    Column {
        TopAppBar(
            title = { Text(titulo) },
            navigationIcon = {
                if (onAtras != null) {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar")
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
                IconButton(onClick = pendiente) {
                    Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "Ayuda contextual")
                }
                IconButton(onClick = pendiente) {
                    BadgedBox(badge = { Badge { Text("3") } }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones, 3 sin leer")
                    }
                }
            },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PantallaPendiente(destino: Destino, usuario: Usuario, onCerrarSesion: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sección en construcción", style = MaterialTheme.typography.titleMedium)
                Text(destino.cubre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (destino == perfil) {
            Text(usuario.nombre, style = MaterialTheme.typography.headlineMedium)
            Text("${usuario.rol.nombre} · ${usuario.detalle}", style = MaterialTheme.typography.bodyLarge)
            Text(usuario.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BotonSecundario(onClick = onCerrarSesion, shape = MaterialTheme.shapes.small) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}
