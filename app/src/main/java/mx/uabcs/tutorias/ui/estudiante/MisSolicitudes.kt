@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package mx.uabcs.tutorias.ui.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.theme.TemaUabcs

private val FILTROS = listOf(
    null to "Todas",
    Estado.PENDIENTE to "Pendientes",
    Estado.CONFIRMADA to "Confirmadas",
    Estado.RECHAZADA to "Rechazadas",
    Estado.CANCELADA to "Canceladas",
    Estado.LISTA_ESPERA to "En lista de espera",
)

@Composable
fun MisSolicitudes(calendarioInicial: Boolean, onDetalle: (String) -> Unit, onMensajes: () -> Unit, modifier: Modifier = Modifier) {
    var pestana by rememberSaveable(calendarioInicial) { mutableIntStateOf(if (calendarioInicial) 1 else 0) }
    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background) {
            Tab(pestana == 0, { pestana = 0 }, text = { Text("Lista") })
            Tab(pestana == 1, { pestana = 1 }, text = { Text("Mi calendario") })
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (pestana == 0) Lista(onDetalle, onMensajes) else Calendario(onDetalle, onMensajes)
        }
    }
}

@Composable
private fun Lista(onDetalle: (String) -> Unit, onMensajes: () -> Unit) {
    var filtro by rememberSaveable { mutableStateOf<Estado?>(null) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FILTROS.forEach { (estado, nombre) -> FilterChip(filtro == estado, { filtro = estado }, { Text(nombre) }) }
    }
    val visibles = Tutorias.solicitudes
        .filter { filtro == null || it.estado == filtro }
        .sortedWith(compareBy({ !it.activa }, { it.inicio ?: LocalDateTime.MAX }))
    if (visibles.isEmpty()) Text("No hay solicitudes con este filtro.", style = MaterialTheme.typography.bodyMedium)
    visibles.forEach { s -> TarjetaCita(s, onDetalle = { onDetalle(s.id) }, onMensajes = onMensajes) }
}

@Composable
private fun Calendario(onDetalle: (String) -> Unit, onMensajes: () -> Unit) {
    val citas = Tutorias.solicitudes.filter { it.activa && it.inicio != null }
    val primera = citas.mapNotNull { it.inicio?.toLocalDate() }.minOrNull() ?: LocalDate.now()
    var mes by rememberSaveable { mutableStateOf(YearMonth.from(primera)) }
    var dia by rememberSaveable { mutableStateOf(primera) }
    CalendarioMes(mes, { mes = it }, citas.mapNotNull { it.inicio?.toLocalDate() }.toSet(), dia, { dia = it }, "Días con citas")
    val delDia = citas.filter { it.inicio?.toLocalDate() == dia }
    Text(fechaLarga(dia), style = MaterialTheme.typography.titleMedium)
    if (delDia.isEmpty()) Text("Sin citas este día.", style = MaterialTheme.typography.bodyMedium)
    delDia.forEach { s -> TarjetaCita(s, onDetalle = { onDetalle(s.id) }, onMensajes = onMensajes) }
}

@Composable
fun DetalleCita(
    id: String,
    onCambiar: () -> Unit,
    onMensajes: () -> Unit,
    onCancelada: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = Tutorias.solicitud(id) ?: return
    val t = Tutorias.tutor(s.tutorId)
    var confirmar by rememberSaveable { mutableStateOf(false) }
    val peligro = MaterialTheme.colorScheme.error

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Tarjeta {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                BadgeEstado(s.estado)
            }
            DatoFila("Tutor", t.nombre)
            DatoFila("Tipo", "${s.tipo.nombre} · ${formatoTexto(s)}")
            DatoFila("Modalidad", lugar(s, t))
            DatoFila("Fecha y hora", s.inicio?.let(::fechaHora) ?: "Sin fecha · ${s.nota.orEmpty()}")
            DatoFila("Tema", s.tema)
            if (s.estado == Estado.RECHAZADA || s.nota?.startsWith("Cambio") == true) DatoFila("Nota", s.nota.orEmpty())
            s.archivo?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (s.activa || s.estado == Estado.LISTA_ESPERA) {
            Tarjeta(titulo = "Acciones") {
                if (s.activa) AccionFila(Icons.Outlined.Schedule, "Cambiar horario", onClick = onCambiar)
                AccionFila(Icons.AutoMirrored.Outlined.Chat, "Mensajes con el tutor", onClick = onMensajes)
                AccionFila(
                    Icons.Outlined.EventBusy,
                    if (s.estado == Estado.LISTA_ESPERA) "Salir de la lista de espera" else "Cancelar cita",
                    color = peligro,
                    onClick = { confirmar = true },
                )
            }
        }
        Tarjeta(titulo = "Privacidad de esta sesión") {
            Row(
                Modifier.toggleable(
                    value = s.coordinacionPuedeVer,
                    role = Role.Switch,
                    onValueChange = { Tutorias.permitirCoordinacion(id, it) },
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Permitir que Coordinación de Tutorías vea el contenido académico de esta sesión",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = s.coordinacionPuedeVer, onCheckedChange = null)
            }
            Text(
                "Desactivado por defecto. Coordinación solo verá fecha, tutor y asistencia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            icon = { Icon(Icons.Outlined.EventBusy, null, tint = peligro) },
            title = { Text("¿Cancelar esta cita?") },
            text = { Text("Se notificará al tutor y el horario quedará libre para otros estudiantes.") },
            confirmButton = {
                Button(
                    onClick = { confirmar = false; Tutorias.cancelar(id); onCancelada() },
                    colors = ButtonDefaults.buttonColors(containerColor = peligro, contentColor = MaterialTheme.colorScheme.onError),
                ) { Text("Sí, cancelar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("No, mantener") } },
        )
    }
}

@Composable
private fun AccionFila(icono: ImageVector, texto: String, color: Color = Color.Unspecified, onClick: () -> Unit) {
    Column {
        Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val tinte = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
                Icon(icono, null, tint = tinte)
                Text(texto, style = MaterialTheme.typography.bodyLarge, color = tinte, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun CambiarHorario(id: String, onListo: (String) -> Unit, modifier: Modifier = Modifier) {
    val s = Tutorias.solicitud(id) ?: return
    val t = Tutorias.tutor(s.tutorId)
    var pedirAlTutor by rememberSaveable { mutableStateOf(false) }
    var nuevo by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }
    var mensaje by rememberSaveable { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = MaterialTheme.shapes.medium, color = TemaUabcs.colores.infoSuave) {
            Text(
                "Horario actual: ${s.inicio?.let { "${fechaCorta(it.toLocalDate())} · ${hora(it)}" } ?: "sin fecha"} con ${t.nombre}",
                Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text("¿Cómo quieres cambiarla?", style = MaterialTheme.typography.headlineSmall)
        OpcionTarjeta(
            "Elegir otro horario disponible",
            if (t.confirmaAutomatico) "Se confirma de inmediato si está libre." else "Queda pendiente hasta que el tutor acepte.",
            !pedirAlTutor, { pedirAlTutor = false },
        )
        OpcionTarjeta("Enviar solicitud de cambio al tutor", "Propón horarios y el tutor decide.", pedirAlTutor, { pedirAlTutor = true })

        if (!pedirAlTutor) {
            VistaHorarios(t, nuevo, onElegir = { nuevo = it })
            val elegido = nuevo
            Button(
                onClick = {
                    try {
                        Tutorias.cambiarHorario(id, elegido!!)
                        onListo(if (t.confirmaAutomatico) "Horario actualizado." else "Cambio enviado. Falta que el tutor acepte.")
                    } catch (e: IllegalArgumentException) {
                        nuevo = null
                        onListo("Ese horario ya no está libre. Elige otro.")
                    }
                },
                enabled = elegido != null,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(elegido?.let { "Confirmar nuevo horario: ${fechaCorta(it.toLocalDate())}, ${hora(it)}" } ?: "Elige un horario")
            }
        } else {
            CampoTexto(
                etiqueta = "Mensaje para el tutor",
                valor = mensaje,
                onCambio = { mensaje = it },
                placeholder = "Ej. Tengo examen a esa hora. ¿Podría ser el viernes por la tarde?",
                ayuda = "Indica el motivo y los horarios que te acomodan.",
                minLineas = 3,
            )
            Button(
                onClick = { Tutorias.solicitarCambio(id, mensaje); onListo("Solicitud de cambio enviada al tutor.") },
                enabled = mensaje.trim().length >= 10,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Enviar solicitud de cambio") }
        }
    }
}
