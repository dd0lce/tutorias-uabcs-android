package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.AvisosInstitucionales
import mx.uabcs.tutorias.ui.Encabezado
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.fechaLarga
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.hora
import mx.uabcs.tutorias.ui.Fila

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
    val lunes = lunesEditable(hoy)
    val semana = if (lunes.isAfter(hoy)) "la próxima semana" else "esta semana"
    // Cada bloque de horario dura 1 h.
    val horasLibres = t.horarios.count { it.isAfter(ahora) && it.toLocalDate() in lunes..lunes.plusDays(6) && !Tutorias.ocupado(tutorId, it) }

    Pantalla(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Hola, ${t.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineLarge)
            Text(fechaLarga(hoy), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Tarjeta(titulo = "Por atender") {
            Fila(
                Iconos.bandeja,
                if (nuevas.size == 1) "1 solicitud nueva" else "${nuevas.size} solicitudes nuevas",
                nuevas.firstOrNull()?.inicio?.let { "La más próxima: ${fechaCorta(it.toLocalDate())}" } ?: "Nada por responder",
                onSolicitudes,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Fila(
                Iconos.editar,
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
                    Fila(
                        Iconos.reloj,
                        "${hora(checkNotNull(s.inicio))} · ${s.estudiante?.nombre ?: s.materia}",
                        listOfNotNull(
                            s.estudiante?.detalle?.takeIf(String::isNotBlank),
                            s.materia,
                            s.modalidad.nombre,
                            formatoTexto(s),
                        ).joinToString(" · "),
                    ) { onDetalle(s.id) }
                }
            }
        }
        Tarjeta(titulo = "Mi horario") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Iconos.calendario, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("$horasLibres h libres $semana", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onHorario) { Text("Editar") }
            }
        }
        AvisosInstitucionales()
    }
}

