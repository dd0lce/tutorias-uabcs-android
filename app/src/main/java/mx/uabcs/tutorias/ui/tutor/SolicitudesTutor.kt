package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonPrimario
import mx.uabcs.tutorias.ui.estudiante.AvatarIniciales
import mx.uabcs.tutorias.ui.estudiante.BadgeEstado
import mx.uabcs.tutorias.ui.estudiante.Pantalla
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.hora

private val MOTIVOS_RECHAZO = listOf(
    "No tengo disponibilidad ese día",
    "El tema no es de mi área",
    "Ya tengo el cupo lleno esa semana",
    "Otro",
)

@Composable
fun SolicitudesTutor(tutorId: String, onDetalle: (String) -> Unit, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    val mias = Tutorias.solicitudes.filter { it.tutorId == tutorId }
    val nuevas = mias.filter { it.estado == Estado.PENDIENTE }.sortedBy { it.inicio }
    val aceptadas = mias.filter { it.estado == Estado.CONFIRMADA }.sortedBy { it.inicio }
    val historial = mias.filter { !it.activa }.sortedByDescending { it.inicio }
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var rechazando by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background) {
            Tab(pestana == 0, { pestana = 0 }, text = { Text("Nuevas (${nuevas.size})") })
            Tab(pestana == 1, { pestana = 1 }, text = { Text("Aceptadas") })
            Tab(pestana == 2, { pestana = 2 }, text = { Text("Historial") })
        }
        Pantalla() {
            val (lista, vacio) = when (pestana) {
                0 -> nuevas to "No tienes solicitudes por responder."
                1 -> aceptadas to "No tienes solicitudes aceptadas."
                else -> historial to "Todavía no hay historial."
            }
            if (lista.isEmpty()) {
                Text(vacio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            lista.forEach { s ->
                if (s.estado == Estado.PENDIENTE) {
                    Tarjeta {
                        DatosSolicitud(s)
                        BotonPrimario(
                            onClick = { Tutorias.aceptar(s.id); avisar("Solicitud aceptada. La encuentras en Sesiones.") },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Aceptar") }
                        TextButton(onClick = { rechazando = s.id }, modifier = Modifier.fillMaxWidth()) { Text("Rechazar") }
                    }
                } else {
                    Tarjeta(onClick = { onDetalle(s.id) }) { DatosSolicitud(s) }
                }
            }
        }
    }

    rechazando?.let { id ->
        DialogoMotivo(
            titulo = "¿Rechazar la solicitud?",
            explicacion = "El estudiante recibirá una notificación con el motivo.",
            boton = "Rechazar solicitud",
            motivos = MOTIVOS_RECHAZO,
            onCerrar = { rechazando = null },
            onConfirmar = { motivo ->
                Tutorias.rechazar(id, motivo)
                rechazando = null
                avisar("Solicitud rechazada. El estudiante verá el motivo.")
            },
        )
    }
}

@Composable
private fun DatosSolicitud(s: Solicitud) {
    s.estudiante?.let { estudiante ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarIniciales(inicialesEstudiante(estudiante.nombre), tamano = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(estudiante.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (estudiante.detalle.isNotBlank()) {
                    Text(
                        estudiante.detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${s.materia} · ${s.tema}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        BadgeEstado(s.estado)
    }
    Text(
        listOfNotNull(
            s.inicio?.let { "${fechaCorta(it.toLocalDate())} · ${hora(it)}" } ?: "Sin fecha",
            s.modalidad.nombre,
            formatoTexto(s).takeIf { s.formato == Formato.GRUPAL },
        ).joinToString(" · "),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(s.objetivo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    s.archivo?.let {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
    s.nota?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

internal fun inicialesEstudiante(nombre: String): String =
    nombre.trim().split(Regex("\\s+")).mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
