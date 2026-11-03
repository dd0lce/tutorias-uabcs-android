package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Tutor
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.PieLegal
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun TutoresCoordinacion(
    modifier: Modifier = Modifier,
    onTutor: (String) -> Unit = {},
) {
    var textoBusqueda by remember { mutableStateOf("") }

    val todos = Tutorias.tutores
    val tutoresFiltrados = remember(textoBusqueda, todos) {
        if (textoBusqueda.isBlank()) {
            todos
        } else {
            val q = textoBusqueda.trim().lowercase()
            todos.filter { tutor ->
                tutor.nombre.lowercase().contains(q) ||
                    tutor.area.lowercase().contains(q) ||
                    tutor.resumen.lowercase().contains(q) ||
                    tutor.materias.any { it.lowercase().contains(q) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Campo de búsqueda funcional
        CampoTexto(
            etiqueta = "Buscar tutor",
            valor = textoBusqueda,
            onCambio = { textoBusqueda = it },
            placeholder = "Nombre, materia o carrera",
            icono = Icons.Outlined.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        // Contador de tutores activos
        Text(
            text = "Tutores activos (${tutoresFiltrados.size} de 24)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        // Lista de tarjetas de tutores
        if (tutoresFiltrados.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "No se encontraron tutores que coincidan con la búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            tutoresFiltrados.forEach { tutor ->
                TarjetaTutorCoordinacion(
                    tutor = tutor,
                    onClick = { onTutor(tutor.id) },
                )
            }
        }

        // Pie Legal
        PieLegal()
    }
}

@Composable
private fun TarjetaTutorCoordinacion(
    tutor: Tutor,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Círculo con Iniciales
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tutor.iniciales,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Datos principales del tutor
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = tutor.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = tutor.resumen,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Calificación, sesiones y área/carrera
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = Color(0xFFC9A227),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${tutor.calificacion} · ${tutor.sesiones} sesiones · ${tutor.area}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Modalidades y formatos (Texto destacado)
                Text(
                    text = modalidadesFormatosTexto(tutor),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Estadísticas del mes
                Text(
                    text = estadisticasMes(tutor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Ícono de flecha (>)
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun modalidadesFormatosTexto(tutor: Tutor): String {
    val mods = tutor.modalidades.map {
        when (it) {
            Modalidad.PRESENCIAL -> "Presencial"
            Modalidad.EN_LINEA -> "En línea"
        }
    }
    val forms = if (tutor.formatos.contains(Formato.INDIVIDUAL) && tutor.formatos.contains(Formato.GRUPAL)) {
        "Individual / Grupal"
    } else if (tutor.formatos.contains(Formato.INDIVIDUAL)) {
        "Individual"
    } else {
        "Grupal"
    }
    return (mods + forms).joinToString(" · ")
}

private fun estadisticasMes(tutor: Tutor): String = when (tutor.id) {
    "mariana" -> "Este mes: 14 sesiones · 2 pendientes"
    "sofia" -> "Este mes: 6 sesiones · sin pendientes"
    "luis" -> "Este mes: 12 sesiones · 1 pendiente"
    "ruben" -> "Este mes: 4 sesiones · sin pendientes"
    else -> "Este mes: ${tutor.sesiones / 2} sesiones · sin pendientes"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TutoresCoordinacionPreview() {
    TutoriasTheme {
        TutoresCoordinacion()
    }
}
