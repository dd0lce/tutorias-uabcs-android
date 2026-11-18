package mx.uabcs.tutorias.ui.tutor

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.hora
import mx.uabcs.tutorias.ui.Fila

@Composable
fun SesionesTutor(tutorId: String, onDetalle: (String) -> Unit, modifier: Modifier = Modifier) {
    val proximas = Tutorias.proximas().filter { it.tutorId == tutorId && it.estado == Estado.CONFIRMADA }

    Pantalla(modifier) {
        Seccion("Por registrar", "No tienes sesiones pasadas por registrar.", Tutorias.porRegistrar(tutorId), Iconos.editar, onDetalle) {
            "${cuando(it)} · ya pasó"
        }
        Seccion("Próximas", "No tienes sesiones confirmadas próximas.", proximas, Iconos.reloj, onDetalle) {
            "${cuando(it)} · ${it.modalidad.nombre}"
        }
        Seccion("Completadas", "Aún no registras sesiones.", Tutorias.realizadas(tutorId), Iconos.check, onDetalle) {
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
