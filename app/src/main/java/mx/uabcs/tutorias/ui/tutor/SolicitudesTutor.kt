package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.estudiante.BadgeEstado
import mx.uabcs.tutorias.ui.estudiante.FilaIcono
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.lugar

@Composable
fun SolicitudesTutor(tutorId: String, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    val (pendientes, resto) = Tutorias.solicitudes.filter { it.tutorId == tutorId }
        .partition { it.estado == Estado.PENDIENTE }
    var rechazando by rememberSaveable { mutableStateOf<String?>(null) }
    var motivo by rememberSaveable { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Por responder (${pendientes.size})", style = MaterialTheme.typography.headlineSmall)
        if (pendientes.isEmpty()) {
            Text(
                "No tienes solicitudes por responder.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        pendientes.forEach { s ->
            Tarjeta {
                DatosSolicitud(s)
                if (rechazando == s.id) {
                    CampoTexto(
                        etiqueta = "Motivo del rechazo",
                        valor = motivo,
                        onCambio = { motivo = it },
                        placeholder = "Ej. Ese día tengo junta de academia.",
                        ayuda = "El estudiante verá este motivo.",
                        minLineas = 2,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonSecundario(onClick = { rechazando = null }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                Tutorias.rechazar(s.id, motivo)
                                rechazando = null
                                avisar("Solicitud rechazada. Le avisamos al estudiante.")
                            },
                            enabled = motivo.isNotBlank(),
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Rechazar") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { Tutorias.aceptar(s.id); avisar("Solicitud aceptada. La sesión quedó confirmada.") },
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Aceptar") }
                        BotonSecundario(onClick = { rechazando = s.id; motivo = "" }) { Text("Rechazar") }
                    }
                }
            }
        }
        if (resto.isNotEmpty()) {
            Text("Historial", style = MaterialTheme.typography.headlineSmall)
            resto.forEach { s -> Tarjeta { DatosSolicitud(s) } }
        }
    }
}

@Composable
private fun DatosSolicitud(s: Solicitud) {
    val t = Tutorias.tutor(s.tutorId)
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(s.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        BadgeEstado(s.estado)
    }
    FilaIcono(Icons.Outlined.CalendarToday, s.inicio?.let(::fechaHora) ?: "Sin fecha")
    FilaIcono(if (s.modalidad == Modalidad.PRESENCIAL) Icons.Outlined.Place else Icons.Outlined.Videocam, lugar(s, t))
    FilaIcono(Icons.Outlined.Group, formatoTexto(s))
    Text("Tema: ${s.tema}. ${s.objetivo}", style = MaterialTheme.typography.bodyMedium)
    s.nota?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
