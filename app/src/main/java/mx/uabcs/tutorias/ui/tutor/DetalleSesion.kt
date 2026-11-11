package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.estudiante.BadgeEstado
import mx.uabcs.tutorias.ui.estudiante.DatoFila
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.lugar

private val MOTIVOS = listOf("Junta de academia a esa hora", "Se me empalmó otra actividad", "Estoy enfermo/a", "Otro")

private fun Solicitud.yaPaso() = inicio?.isBefore(Tutorias.ahora()) == true

@Composable
fun DetalleSesion(
    id: String,
    onRegistrar: () -> Unit,
    onMensajes: () -> Unit,
    onCancelada: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = Tutorias.solicitud(id) ?: return
    val t = Tutorias.tutor(s.tutorId)
    var cancelando by rememberSaveable { mutableStateOf(false) }
    val confirmada = s.estado == Estado.CONFIRMADA

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Tarjeta {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                BadgeEstado(s.estado)
            }
            // shortcut: Solicitud no guarda al estudiante; agregar la fila "Estudiante" cuando el modelo lo tenga.
            DatoFila("Formato", "${s.tipo.nombre} · ${formatoTexto(s)}")
            DatoFila("Dónde", lugar(s, t))
            DatoFila("Cuándo", s.inicio?.let(::fechaHora) ?: "Sin fecha")
            DatoFila("Tema", s.tema)
            DatoFila("Objetivo", s.objetivo)
            s.nota?.let { DatoFila("Nota", it) }
            s.asistio?.let { DatoFila("Asistencia", if (it) "Asistió" else "No asistió") }
            s.notasSesion?.let { DatoFila("Acuerdos", it) }
            s.archivo?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (confirmada) {
            Tarjeta {
                if (s.yaPaso()) {
                    Button(
                        onClick = onRegistrar,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) { Text("Registrar sesión") }
                }
                BotonSecundario(onClick = onMensajes, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Mensaje al estudiante")
                }
                if (!s.yaPaso()) {
                    TextButton(onClick = { cancelando = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar sesión")
                    }
                }
            }
        }
    }

    if (cancelando) DialogoCancelar(id, onCerrar = { cancelando = false }, onCancelada = onCancelada)
}

@Composable
private fun DialogoCancelar(id: String, onCerrar: () -> Unit, onCancelada: () -> Unit) {
    var motivo by rememberSaveable { mutableStateOf(MOTIVOS.first()) }
    var otro by rememberSaveable { mutableStateOf("") }
    val texto = if (motivo == MOTIVOS.last()) otro else motivo
    val peligro = MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Outlined.EventBusy, null, tint = peligro) },
        title = { Text("¿Cancelar la sesión?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("El estudiante recibirá una notificación con el motivo. El horario queda libre para otros estudiantes.")
                Column(Modifier.selectableGroup()) {
                    MOTIVOS.forEach { m ->
                        Row(
                            Modifier.fillMaxWidth().selectable(m == motivo, role = Role.RadioButton, onClick = { motivo = m }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = m == motivo, onClick = null)
                            Text(m, Modifier.padding(start = 8.dp))
                        }
                    }
                }
                if (motivo == MOTIVOS.last()) {
                    CampoTexto(etiqueta = "Motivo", valor = otro, onCambio = { otro = it }, placeholder = "Escribe el motivo")
                }
                Text(
                    "El estudiante verá este motivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { Tutorias.cancelarComoTutor(id, texto); onCerrar(); onCancelada() },
                enabled = texto.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = peligro, contentColor = MaterialTheme.colorScheme.onError),
            ) { Text("Cancelar sesión") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Volver") } },
    )
}

@Composable
fun RegistrarSesion(id: String, onListo: () -> Unit, modifier: Modifier = Modifier) {
    val s = Tutorias.solicitud(id) ?: return
    var asistio by rememberSaveable { mutableStateOf(true) }
    var acuerdos by rememberSaveable { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Tarjeta {
            Text(s.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(
                s.inicio?.let(::fechaHora).orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth().toggleable(asistio, role = Role.Checkbox, onValueChange = { asistio = it }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = asistio, onCheckedChange = null)
                Text("El estudiante asistió", Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        CampoTexto(
            etiqueta = "Acuerdos (opcional)",
            valor = acuerdos,
            onCambio = { acuerdos = it },
            placeholder = "1. Repasar la regla de L'Hôpital.\n2. Resolver ejercicios 4–8 de la guía.",
            minLineas = 4,
        )
        Button(
            onClick = { Tutorias.registrarSesion(id, asistio, acuerdos); onListo() },
            enabled = s.estado == Estado.CONFIRMADA && s.yaPaso(),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Guardar registro") }
    }
}
