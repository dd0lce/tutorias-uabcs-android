package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.estudiante.Aviso
import mx.uabcs.tutorias.ui.estudiante.Encabezado
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.fechaLarga
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.hora

@Composable
fun InicioTutor(
    tutorId: String,
    onSolicitudes: () -> Unit,
    onSesiones: () -> Unit,
    onDetalle: (String) -> Unit,
    onHorario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = Tutorias.tutor(tutorId)
    val ahora = Tutorias.ahora()
    val hoy = ahora.toLocalDate()
    val mias = Tutorias.solicitudes.filter { it.tutorId == tutorId }
    val nuevas = mias.filter { it.estado == Estado.PENDIENTE }.sortedBy { it.inicio }
    val porRegistrar = Tutorias.porRegistrar(tutorId)
    val deHoy = mias.filter { it.estado == Estado.CONFIRMADA && it.inicio?.toLocalDate() == hoy && it.inicio?.isAfter(ahora) == true }
        .sortedBy { it.inicio }
    val finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    // Cada bloque de horario dura 1 h.
    val horasLibres = t.horarios.count { it.isAfter(ahora) && !it.toLocalDate().isAfter(finSemana) && !Tutorias.ocupado(tutorId, it) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Hola, ${t.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineLarge)
            Text(fechaLarga(hoy), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Tarjeta(titulo = "Por atender") {
            Fila(
                Icons.Outlined.Inbox,
                if (nuevas.size == 1) "1 solicitud nueva" else "${nuevas.size} solicitudes nuevas",
                nuevas.firstOrNull()?.inicio?.let { "La más próxima: ${fechaCorta(it.toLocalDate())}" } ?: "Nada por responder",
                onSolicitudes,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Fila(
                Icons.Outlined.Edit,
                if (porRegistrar.size == 1) "1 sesión sin registrar" else "${porRegistrar.size} sesiones sin registrar",
                porRegistrar.firstOrNull()?.let { s -> "${s.materia} · ${s.inicio?.let { fechaCorta(it.toLocalDate()) }}" }
                    ?: "Todo registrado",
                onSesiones,
            )
        }
        Encabezado("Hoy", "Ver todas", onSesiones)
        if (deHoy.isEmpty()) {
            Text(
                "No tienes sesiones hoy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Tarjeta {
                deHoy.forEachIndexed { i, s ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    // shortcut: sin datos del estudiante en Solicitud se muestra la materia; cambiar al nombre cuando exista.
                    Fila(
                        Icons.Outlined.Schedule,
                        "${hora(checkNotNull(s.inicio))} · ${s.materia}",
                        "${s.modalidad.nombre} · ${formatoTexto(s)}",
                    ) { onDetalle(s.id) }
                }
            }
        }
        Tarjeta(titulo = "Mi horario") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("$horasLibres h libres esta semana", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onHorario) { Text("Editar") }
            }
        }
        Tarjeta(titulo = "Avisos") {
            Aviso("Tutorías de regularización de Cálculo", "Del 19 al 30 de octubre en Biblioteca Central.")
        }
    }
}

@Composable
internal fun Fila(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icono, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
