@file:OptIn(ExperimentalLayoutApi::class)

package mx.uabcs.tutorias.ui.estudiante

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.theme.TemaUabcs

private const val REQUISITOS = """1. Indica el tema y el objetivo de la sesión.
2. Adjunta ejercicios o avances (PDF o imagen).
3. Solicita con al menos 24 h de anticipación.
4. Grupal: mínimo 2 y máximo 5 estudiantes."""

@Composable
fun PerfilTutor(tutorId: String, onSolicitar: (LocalDateTime?) -> Unit, modifier: Modifier = Modifier) {
    val t = Tutorias.tutor(tutorId)
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Tarjeta {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(t.iniciales, tamano = 88.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t.nombre, style = MaterialTheme.typography.headlineMedium)
                    Text("${t.titulo} · ${t.area}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.StarOutline, null, Modifier.size(16.dp), tint = TemaUabcs.colores.advertencia)
                        Text("${t.calificacion} · ${t.sesiones} sesiones", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Tarjeta(titulo = "Horarios") {
            VistaHorarios(t, seleccionado = null, onElegir = { onSolicitar(it) })
        }
        Button(onClick = { onSolicitar(null) }, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Solicitar sesión")
        }
        Tarjeta(titulo = "Materias y áreas") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                t.materias.forEach {
                    Surface(shape = CircleShape, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                        Text(it, Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Tarjeta(titulo = "Modalidades") {
            if (Modalidad.PRESENCIAL in t.modalidades) Text("Presencial · ${t.lugar}", style = MaterialTheme.typography.bodyLarge)
            if (Modalidad.EN_LINEA in t.modalidades) Text("En línea · Google Meet institucional", style = MaterialTheme.typography.bodyLarge)
            Text(
                when (t.formatos) {
                    setOf(Formato.INDIVIDUAL) -> "Solo individual"
                    setOf(Formato.GRUPAL) -> "Solo grupal (máx. 5 estudiantes)"
                    else -> "Individual y grupal (máx. 5 estudiantes)"
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Tarjeta(titulo = "Requisitos para solicitar una sesión") {
            Text(REQUISITOS, style = MaterialTheme.typography.bodyMedium)
        }
        Tarjeta(titulo = "Experiencia académica") {
            Text(t.experiencia.joinToString("\n") { "• $it" }, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
