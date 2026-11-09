package mx.uabcs.tutorias.ui.notificaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Aviso
import mx.uabcs.tutorias.data.TipoAviso
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.theme.TemaUabcs

@Composable
fun Notificaciones(usuario: Usuario, modifier: Modifier = Modifier) {
    val avisos = Tutorias.avisos(usuario)
    val nuevos = remember { avisos.map { it.id }.filter { it !in Tutorias.leidos }.toSet() }
    LaunchedEffect(Unit) { Tutorias.marcarLeidos(usuario) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (avisos.isEmpty()) {
            Text(
                "No tienes notificaciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        avisos.forEach { TarjetaAviso(it, nuevo = it.id in nuevos) }
    }
}

@Composable
private fun TarjetaAviso(a: Aviso, nuevo: Boolean) {
    val s = a.solicitud
    val t = Tutorias.tutor(s.tutorId)
    val c = TemaUabcs.colores
    val cuando = s.inicio?.let(::fechaHora).orEmpty()
    val (icono, color, titulo, detalle) = when (a.tipo) {
        TipoAviso.CONFIRMADA -> Cuarteta(Icons.Outlined.CheckCircle, c.exito, "Sesión confirmada", "${s.titulo} con ${t.nombre} · $cuando")
        TipoAviso.RECHAZADA -> Cuarteta(Icons.Outlined.EventBusy, MaterialTheme.colorScheme.error, "Solicitud rechazada", "${t.nombre} rechazó ${s.titulo}. ${s.nota.orEmpty()}")
        TipoAviso.LISTA_ESPERA -> Cuarteta(Icons.Outlined.HourglassTop, c.espera, "Lista de espera", "Con ${t.nombre}: ${s.nota.orEmpty()}.")
        TipoAviso.NUEVA -> Cuarteta(Icons.Outlined.MarkEmailUnread, MaterialTheme.colorScheme.primary, "Solicitud nueva", "${s.titulo} · $cuando")
        TipoAviso.CAMBIO -> Cuarteta(Icons.Outlined.EditCalendar, c.advertencia, "Piden cambio de horario", "${s.titulo}. ${s.nota.orEmpty()}")
        TipoAviso.POR_REGISTRAR -> Cuarteta(Icons.Outlined.HowToReg, c.advertencia, "Sesión por registrar", "${s.titulo} · $cuando")
    }
    Tarjeta {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icono, contentDescription = null, tint = color)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
            }
            if (nuevo) Badge { Text("Nuevo") }
        }
    }
}

private data class Cuarteta<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
