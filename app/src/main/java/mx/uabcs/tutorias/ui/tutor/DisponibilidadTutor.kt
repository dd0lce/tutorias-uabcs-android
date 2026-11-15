package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Franja
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Tutor
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.alternar
import mx.uabcs.tutorias.ui.estudiante.diaCorto
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.estudiante.fechaLarga
import mx.uabcs.tutorias.ui.estudiante.hora

private val HORAS = Franja.entries.flatMap { it.horas }

@Composable
fun DisponibilidadTutor(tutorId: String, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    val t = Tutorias.tutor(tutorId)
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HorarioSemanal(t, avisar)
        Materias(t, avisar)
        Modalidades(t, avisar)
    }
}

@Composable
private fun HorarioSemanal(t: Tutor, avisar: (String) -> Unit) {
    val ahora = Tutorias.ahora()
    val hoy = ahora.toLocalDate()
    // En fin de semana la semana actual ya pasó; se empieza en el lunes siguiente.
    val primerLunes = if (hoy.dayOfWeek >= DayOfWeek.SATURDAY) hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    else hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    var lunes by rememberSaveable { mutableStateOf(primerLunes) }
    val dias = (0L..4L).map { lunes.plusDays(it) }
    val editable = { h: LocalDateTime -> h.isAfter(ahora) && h.toLocalDate() in dias }
    val guardados = t.horarios.filter(editable).toSet()
    var borrador by rememberSaveable(lunes, guardados) { mutableStateOf(guardados) }
    val hayCambios = borrador != guardados

    Tarjeta(titulo = "Horario semanal") {
        Text(
            "Toca un bloque para abrirlo o cerrarlo. Los estudiantes solo ven horarios con al menos 24 h de anticipación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(primerLunes to "Esta semana", primerLunes.plusWeeks(1) to "Próxima semana").forEach { (l, nombre) ->
                FilterChip(selected = lunes == l, onClick = { lunes = l }, label = { Text(nombre) })
            }
        }
        Text("${fechaCorta(dias.first())} – ${fechaCorta(dias.last())}", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.width(44.dp))
            dias.forEach { d ->
                Text(diaCorto(d), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
        HORAS.forEach { hr ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$hr:00", Modifier.width(44.dp), style = MaterialTheme.typography.labelSmall)
                dias.forEach { d ->
                    val h = d.atTime(hr, 0)
                    Bloque(
                        h,
                        abierto = h in borrador,
                        agendado = Tutorias.ocupado(t.id, h),
                        habilitado = editable(h),
                        onCambio = { borrador = borrador.alternar(h) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (hayCambios) {
            Text("Tienes cambios sin guardar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { Tutorias.guardarSemana(t.id, lunes, borrador); avisar("Horario guardado.") },
            enabled = hayCambios,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Guardar cambios") }
        if (lunes == primerLunes) {
            BotonSecundario(
                onClick = {
                    Tutorias.guardarSemana(t.id, lunes, borrador)
                    Tutorias.copiarSemana(t.id, lunes)
                    avisar("Horario copiado a la próxima semana.")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Copiar a la próxima semana") }
        }
    }
}

@Composable
private fun Bloque(h: LocalDateTime, abierto: Boolean, agendado: Boolean, habilitado: Boolean, onCambio: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val (fondo, borde) = when {
        agendado -> c.primary to c.primary
        abierto -> c.primaryContainer to c.primary
        else -> c.surface to c.outlineVariant
    }
    val estado = when {
        agendado -> "con sesión agendada"
        abierto -> "abierto"
        else -> "cerrado"
    }
    Box(
        modifier.height(36.dp).clip(MaterialTheme.shapes.small).background(fondo)
            .border(BorderStroke(1.dp, borde), MaterialTheme.shapes.small)
            // Una hora con sesión agendada no se puede cerrar.
            .toggleable(abierto, enabled = habilitado && !agendado, role = Role.Checkbox, onValueChange = { onCambio() })
            .semantics { contentDescription = "${fechaLarga(h.toLocalDate())}, ${hora(h)}, $estado" },
        contentAlignment = Alignment.Center,
    ) {
        val icono = when {
            agendado -> Icons.Outlined.Event
            abierto -> Icons.Outlined.Check
            else -> Icons.Outlined.Add
        }
        val tinte = when {
            agendado -> c.onPrimary
            !habilitado -> c.outlineVariant
            abierto -> c.primary
            else -> c.onSurfaceVariant
        }
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Materias(t: Tutor, avisar: (String) -> Unit) {
    var agregando by rememberSaveable { mutableStateOf(false) }
    var nueva by rememberSaveable { mutableStateOf("") }

    Tarjeta(titulo = "Materias y áreas que atiendo") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            t.materias.forEach { m ->
                InputChip(
                    selected = true,
                    onClick = {
                        if (t.materias.size > 1) Tutorias.quitarMateria(t.id, m) else avisar("Debes atender al menos una materia.")
                    },
                    label = { Text(m) },
                    trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Quitar $m", Modifier.size(18.dp)) },
                )
            }
        }
        TextButton(onClick = { agregando = true; nueva = "" }, modifier = Modifier.fillMaxWidth()) { Text("+ Agregar materia") }
    }

    if (agregando) {
        val repetida = t.materias.any { it.equals(nueva.trim(), ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { agregando = false },
            title = { Text("Agregar materia") },
            text = {
                CampoTexto(
                    etiqueta = "Materia o área",
                    valor = nueva,
                    onCambio = { nueva = it },
                    placeholder = "Ej. Ecuaciones diferenciales",
                    ayuda = if (repetida) "Ya atiendes esa materia." else null,
                    esError = repetida,
                )
            },
            confirmButton = {
                Button(
                    onClick = { Tutorias.agregarMateria(t.id, nueva); agregando = false },
                    enabled = nueva.isNotBlank() && !repetida,
                ) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { agregando = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Modalidades(t: Tutor, avisar: (String) -> Unit) {
    fun ajustar(modalidades: Set<Modalidad> = t.modalidades, formatos: Set<Formato> = t.formatos, auto: Boolean = t.confirmaAutomatico) {
        if (modalidades.isEmpty()) avisar("Elige al menos una modalidad.")
        else Tutorias.ajustarModalidades(t.id, modalidades, formatos, auto)
    }
    val grupal = Formato.GRUPAL in t.formatos

    Tarjeta(titulo = "Modalidades") {
        Ajuste("Presencial", t.lugar, Modalidad.PRESENCIAL in t.modalidades) { ajustar(modalidades = t.modalidades.alternar(Modalidad.PRESENCIAL)) }
        Ajuste("En línea", "Google Meet", Modalidad.EN_LINEA in t.modalidades) { ajustar(modalidades = t.modalidades.alternar(Modalidad.EN_LINEA)) }
        Ajuste("Sesiones grupales", "Hasta 5 estudiantes", grupal) {
            ajustar(formatos = if (grupal) (t.formatos - Formato.GRUPAL).ifEmpty { setOf(Formato.INDIVIDUAL) } else t.formatos + Formato.GRUPAL)
        }
        Ajuste("Aceptar solicitudes automáticamente", "Si el horario está libre", t.confirmaAutomatico) {
            ajustar(auto = !t.confirmaAutomatico)
        }
    }
}

@Composable
private fun Ajuste(titulo: String, detalle: String, activo: Boolean, onCambio: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().toggleable(activo, role = Role.Switch, onValueChange = { onCambio() }).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = activo, onCheckedChange = null)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
