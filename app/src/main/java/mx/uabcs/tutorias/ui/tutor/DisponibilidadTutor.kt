package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Franja
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.TutoriasRepo
import mx.uabcs.tutorias.ui.estudiante.diaCorto
import mx.uabcs.tutorias.ui.estudiante.fechaLarga
import mx.uabcs.tutorias.ui.estudiante.hora

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisponibilidadTutor(tutorId: String, modifier: Modifier = Modifier) {
    val t = Tutorias.tutor(tutorId)
    val ahora = Tutorias.ahora()
    val dias = (0L until TutoriasRepo.DIAS_AGENDA).map { ahora.toLocalDate().plusDays(it) }
    var dia by rememberSaveable { mutableStateOf(dias.first()) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Toca una hora para abrirla o cerrarla. Los estudiantes solo ven horarios con al menos " +
                "${TutoriasRepo.ANTICIPACION_HORAS} h de anticipación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dias.forEach { d ->
                val abiertos = t.horarios.count { it.toLocalDate() == d }
                FilterChip(
                    selected = d == dia,
                    onClick = { dia = d },
                    label = { Text(if (abiertos > 0) "${diaCorto(d)} · $abiertos" else diaCorto(d)) },
                )
            }
        }
        Text(fechaLarga(dia), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Franja.entries.flatMap { it.horas }.map { dia.atTime(it, 0) }.forEach { h ->
                val ocupado = Tutorias.ocupado(tutorId, h)
                FilterChip(
                    selected = h in t.horarios,
                    onClick = { Tutorias.alternarHorario(tutorId, h) },
                    enabled = !ocupado && h.isAfter(ahora),
                    label = { Text(if (ocupado) "${hora(h)} · agendada" else hora(h)) },
                )
            }
        }
    }
}
