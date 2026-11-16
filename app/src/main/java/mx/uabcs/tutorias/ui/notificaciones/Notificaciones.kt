package mx.uabcs.tutorias.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Aviso
import mx.uabcs.tutorias.data.TipoAviso
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.theme.TemaUabcs

@Composable
fun Notificaciones(usuario: Usuario, onAbrir: (Aviso) -> Unit, modifier: Modifier = Modifier) {
    val avisos = Tutorias.avisos(usuario)
    val sinLeer = Tutorias.sinLeer(usuario)

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
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$sinLeer sin leer", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { Tutorias.marcarLeidos(usuario) }, enabled = sinLeer > 0) { Text("Marcar todas como leídas") }
        }
        Tarjeta {
            avisos.forEachIndexed { i, a ->
                if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilaAviso(a, nuevo = a.id !in Tutorias.leidos) {
                    Tutorias.marcarLeido(a.id)
                    onAbrir(a)
                }
            }
        }
    }
}

@Composable
private fun FilaAviso(a: Aviso, nuevo: Boolean, onClick: () -> Unit) {
    val s = a.solicitud
    val t = Tutorias.tutor(s.tutorId)
    val c = TemaUabcs.colores
    val cuando = s.inicio?.let(::fechaHora).orEmpty()
    val (icono, color, fondo) = when (a.tipo) {
        TipoAviso.CONFIRMADA -> Triple(Icons.Outlined.CheckCircle, c.exito, c.exitoSuave)
        TipoAviso.RECHAZADA -> Triple(Icons.Outlined.EventBusy, c.peligro, c.peligroSuave)
        TipoAviso.LISTA_ESPERA -> Triple(Icons.Outlined.HourglassTop, c.espera, c.esperaSuave)
        TipoAviso.NUEVA -> Triple(Icons.Outlined.MarkEmailUnread, c.info, c.infoSuave)
        TipoAviso.CAMBIO -> Triple(Icons.Outlined.EditCalendar, c.advertencia, c.advertenciaSuave)
        TipoAviso.POR_REGISTRAR -> Triple(Icons.Outlined.HowToReg, c.advertencia, c.advertenciaSuave)
    }
    val (titulo, detalle) = when (a.tipo) {
        TipoAviso.CONFIRMADA -> "Sesión confirmada" to "${s.titulo} con ${t.nombre} · $cuando"
        TipoAviso.RECHAZADA -> "Solicitud rechazada" to "${t.nombre} rechazó ${s.titulo}. ${s.nota.orEmpty()}"
        TipoAviso.LISTA_ESPERA -> "Lista de espera" to "Con ${t.nombre}: ${s.nota.orEmpty()}."
        TipoAviso.NUEVA -> "Nueva solicitud" to "${s.titulo} · $cuando"
        TipoAviso.CAMBIO -> "Piden cambio de horario" to "${s.titulo}. ${s.nota.orEmpty()}"
        TipoAviso.POR_REGISTRAR -> "Tienes una sesión sin registrar" to "${s.titulo} · $cuando"
    }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(fondo), contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, tint = color)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (nuevo) {
            Box(
                Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    .semantics { contentDescription = "Sin leer" },
            )
        }
    }
}
