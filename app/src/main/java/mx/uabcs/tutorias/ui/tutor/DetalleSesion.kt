package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
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
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonPrimario
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.BadgeEstado
import mx.uabcs.tutorias.ui.DatoFila
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.estudiante.formatoTexto
import mx.uabcs.tutorias.ui.estudiante.lugar

private val MOTIVOS_CANCELAR = listOf("Junta de academia a esa hora", "Se me empalmó otra actividad", "Estoy enfermo/a", "Otro")

private fun Solicitud.yaPaso() = inicio?.isBefore(Tutorias.ahora()) == true

internal fun Solicitud.etiquetaEstudiante(): String? = estudiante
    ?.let { listOf(it.nombre, it.detalle).filter(String::isNotBlank).joinToString(" · ") }
    ?.ifBlank { null }

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

    Pantalla(modifier) {
        Tarjeta {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                BadgeEstado(s.estado)
            }
            s.etiquetaEstudiante()?.let { DatoFila("Estudiante", it) }
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
                    Icon(Iconos.archivo, null, tint = MaterialTheme.colorScheme.primary)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (confirmada) {
            Tarjeta {
                if (s.yaPaso()) {
                    BotonPrimario(
                        onClick = onRegistrar,
                        modifier = Modifier.fillMaxWidth(),
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

    if (cancelando) {
        DialogoMotivo(
            titulo = "¿Cancelar la sesión?",
            explicacion = "El estudiante recibirá una notificación con el motivo. El horario queda libre para otros estudiantes.",
            boton = "Cancelar sesión",
            motivos = MOTIVOS_CANCELAR,
            onCerrar = { cancelando = false },
            onConfirmar = { Tutorias.cancelarComoTutor(id, it); cancelando = false; onCancelada() },
        )
    }
}

/** Diálogo para elegir un motivo que verá el estudiante. El último motivo de la lista abre un campo de texto. */
@Composable
internal fun DialogoMotivo(
    titulo: String,
    explicacion: String,
    boton: String,
    motivos: List<String>,
    onCerrar: () -> Unit,
    onConfirmar: (String) -> Unit,
) {
    var motivo by rememberSaveable { mutableStateOf(motivos.first()) }
    var otro by rememberSaveable { mutableStateOf("") }
    val texto = if (motivo == motivos.last()) otro else motivo
    val peligro = MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Iconos.calendario, null, tint = peligro) },
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(explicacion)
                Column(Modifier.selectableGroup()) {
                    motivos.forEach { m ->
                        Row(
                            Modifier.fillMaxWidth().selectable(m == motivo, role = Role.RadioButton, onClick = { motivo = m }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = m == motivo, onClick = null)
                            Text(m, Modifier.padding(start = 8.dp))
                        }
                    }
                }
                if (motivo == motivos.last()) {
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
                onClick = { onConfirmar(texto) },
                enabled = texto.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = peligro, contentColor = MaterialTheme.colorScheme.onError),
            ) { Text(boton) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Volver") } },
    )
}

@Composable
fun RegistrarSesion(id: String, onListo: () -> Unit, modifier: Modifier = Modifier) {
    val s = Tutorias.solicitud(id) ?: return
    var asistio by rememberSaveable { mutableStateOf(true) }
    var acuerdos by rememberSaveable { mutableStateOf("") }

    Pantalla(modifier) {
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
        BotonPrimario(
            onClick = { Tutorias.registrarSesion(id, asistio, acuerdos); onListo() },
            enabled = s.estado == Estado.CONFIRMADA && s.yaPaso(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Guardar registro") }
    }
}
