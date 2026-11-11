package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.hora

@Composable
fun SesionesTutor(tutorId: String, onDetalle: (String) -> Unit, modifier: Modifier = Modifier) {
    val proximas = Tutorias.proximas().filter { it.tutorId == tutorId && it.estado == Estado.CONFIRMADA }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Seccion("Por registrar", "No tienes sesiones pasadas por registrar.", Tutorias.porRegistrar(tutorId), Icons.Outlined.Edit, onDetalle) {
            "${cuando(it)} · ya pasó"
        }
        Seccion("Próximas", "No tienes sesiones confirmadas próximas.", proximas, Icons.Outlined.Schedule, onDetalle) {
            "${cuando(it)} · ${it.modalidad.nombre}"
        }
        Seccion("Completadas", "Aún no registras sesiones.", Tutorias.realizadas(tutorId), Icons.Outlined.Check, onDetalle) {
            "${cuando(it)} · ${if (it.asistio == true) "asistió" else "no asistió"}"
        }
    }
}

private fun cuando(s: Solicitud) = s.inicio?.let { "${fechaCorta(it.toLocalDate())} · ${hora(it)}" } ?: "Sin fecha"

@Composable
private fun Seccion(
    titulo: String,
    vacio: String,
    sesiones: List<Solicitud>,
    icono: ImageVector,
    onDetalle: (String) -> Unit,
    detalle: (Solicitud) -> String,
) {
    Text(titulo, style = MaterialTheme.typography.headlineSmall)
    if (sesiones.isEmpty()) {
        Text(vacio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Tarjeta {
        sesiones.forEachIndexed { i, s ->
            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Fila(icono, s.titulo, detalle(s)) { onDetalle(s.id) }
        }
    }
}
