package mx.uabcs.tutorias.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.uabcs.tutorias.data.Api
import mx.uabcs.tutorias.data.ErrorApi
import mx.uabcs.tutorias.data.Mensaje
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.BadgeEstado
import mx.uabcs.tutorias.ui.FilaIcono
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.estudiante.hora

private const val CADA_MS = 5_000L

@Composable
fun Conversaciones(usuario: Usuario, onAbrir: (String) -> Unit, modifier: Modifier = Modifier) {
    val solicitudes = Tutorias.solicitudes.sortedByDescending { it.inicio ?: LocalDateTime.MIN }
    Pantalla(modifier) {
        if (solicitudes.isEmpty()) {
            Text(
                if (usuario.rol == Rol.TUTOR) "Cuando un estudiante te mande una solicitud podrás escribirle aquí."
                else "Cuando solicites una tutoría podrás escribirle al tutor aquí.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        solicitudes.forEach { s ->
            Tarjeta(onClick = { onAbrir(s.id) }) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    BadgeEstado(s.estado)
                }
                FilaIcono(Iconos.calendario, s.inicio?.let(::fechaHora) ?: "Sin fecha")
                if (usuario.rol == Rol.ESTUDIANTE) FilaIcono(Iconos.perfil, "Con ${Tutorias.tutor(s.tutorId).nombre}")
            }
        }
    }
}

/** Chat de una solicitud. Pregunta por mensajes nuevos cada 5 s mientras está abierto. */
@Composable
fun Chat(solicitudId: String, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    val mensajes = remember(solicitudId) { mutableStateListOf<Mensaje>() }
    var texto by rememberSaveable { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    val lista = rememberLazyListState()
    val alcance = rememberCoroutineScope()

    fun agregar(nuevos: List<Mensaje>) {
        val ids = mensajes.map { it.id }.toSet()
        mensajes += nuevos.filter { it.id !in ids }
    }

    LaunchedEffect(solicitudId) {
        var fallo = false
        while (true) {
            try {
                agregar(Api.mensajes(solicitudId, mensajes.lastOrNull()?.id ?: 0))
                fallo = false
            } catch (e: ErrorApi) {
                if (!fallo) avisar(e.message.orEmpty()) // un aviso por racha de fallos, no cada 5 s
                fallo = true
            }
            delay(CADA_MS)
        }
    }
    LaunchedEffect(mensajes.size) { if (mensajes.isNotEmpty()) lista.animateScrollToItem(mensajes.lastIndex) }

    fun enviar() {
        val t = texto.trim()
        if (t.isEmpty() || enviando) return
        enviando = true
        alcance.launch {
            try {
                agregar(listOf(Api.enviarMensaje(solicitudId, t)))
                texto = ""
            } catch (e: ErrorApi) {
                avisar(e.message.orEmpty())
            } finally {
                enviando = false
            }
        }
    }

    Column(modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = lista,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (mensajes.isEmpty()) {
                item {
                    Text(
                        "Todavía no hay mensajes. Escribe para coordinar la sesión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(mensajes, key = { it.id }) { Burbuja(it) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            CampoTexto(
                etiqueta = "Mensaje",
                valor = texto,
                onCambio = { texto = it.take(2000) },
                placeholder = "Escribe un mensaje",
                teclado = KeyboardOptions(imeAction = ImeAction.Send),
                acciones = KeyboardActions(onSend = { enviar() }),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { enviar() }, enabled = !enviando && texto.isNotBlank()) {
                Icon(Iconos.enviar, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
private fun Burbuja(m: Mensaje) {
    val fondo = if (m.propio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Box(Modifier.fillMaxWidth(), contentAlignment = if (m.propio) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier.widthIn(max = 300.dp).background(fondo, MaterialTheme.shapes.medium).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!m.propio) Text(m.autor, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(m.texto, style = MaterialTheme.typography.bodyLarge)
            Text(
                hora(LocalDateTime.ofInstant(m.enviadoEn, ZoneId.systemDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
