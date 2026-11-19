package mx.uabcs.tutorias.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.uabcs.tutorias.data.Api
import mx.uabcs.tutorias.data.ErrorApi
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.AvatarIniciales
import mx.uabcs.tutorias.ui.AvisoPrivacidadDialogo
import mx.uabcs.tutorias.ui.BotonPrimario
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.Fila
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta

/** Iniciales del nombre sin títulos como "Mtra." o "Dr.". */
private fun iniciales(nombre: String) =
    nombre.split(" ").filter { it.firstOrNull()?.isUpperCase() == true && !it.endsWith(".") }
        .take(2).joinToString("") { it.take(1) }.ifEmpty { "?" }

private fun etiquetaDetalle(rol: Rol) = when (rol) {
    Rol.ESTUDIANTE -> "Carrera y semestre"
    Rol.TUTOR -> "Departamento"
    Rol.COORDINACION -> "Cargo"
}

@Composable
fun Perfil(
    usuario: Usuario,
    onEditar: () -> Unit,
    onHorario: (() -> Unit)?,
    onCerrarSesion: () -> Unit,
    avisar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alcance = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    var verPrivacidad by rememberSaveable { mutableStateOf(false) }
    if (verPrivacidad) AvisoPrivacidadDialogo { verPrivacidad = false }

    Pantalla(modifier) {
        Tarjeta {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(iniciales(usuario.nombre), tamano = 72.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(listOf(usuario.rol.nombre, usuario.detalle).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
                    Text(usuario.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            BotonSecundario(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                Icon(Iconos.editar, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Editar perfil")
            }
        }

        if (onHorario != null) {
            Tarjeta(titulo = "Visibilidad de mis datos") {
                Row(
                    Modifier.fillMaxWidth().toggleable(
                        value = usuario.mostrarCorreo,
                        enabled = !guardando,
                        role = Role.Switch,
                        onValueChange = { nuevo ->
                            guardando = true
                            alcance.launch {
                                try {
                                    Api.actualizarPerfil(usuario.nombre, usuario.detalle, nuevo)
                                } catch (e: ErrorApi) {
                                    avisar(e.message.orEmpty())
                                } finally {
                                    guardando = false
                                }
                            }
                        },
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Mostrar mi correo a los estudiantes", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Aparece en tu perfil de tutor. Nombre y departamento siempre son visibles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = usuario.mostrarCorreo, onCheckedChange = null, enabled = !guardando)
                }
            }
        }

        Tarjeta {
            if (onHorario != null) {
                Fila(Iconos.reloj, "Mi horario de atención", "Abre o cierra tus horas libres.", onHorario)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            Fila(Iconos.escudo, "Aviso de privacidad", "Qué datos guardamos y cómo ejercer tus derechos.") { verPrivacidad = true }
        }

        BotonSecundario(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Icon(Iconos.salir, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun EditarPerfil(usuario: Usuario, onListo: () -> Unit, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    var nombre by rememberSaveable { mutableStateOf(usuario.nombre) }
    var detalle by rememberSaveable { mutableStateOf(usuario.detalle) }
    var guardando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val valido = nombre.trim().length in 3..150 && detalle.trim().length <= 200

    Pantalla(modifier) {
        Tarjeta {
            CampoTexto(etiqueta = "Nombre completo", valor = nombre, onCambio = { nombre = it }, placeholder = "", modifier = Modifier.fillMaxWidth())
            CampoTexto(etiqueta = etiquetaDetalle(usuario.rol), valor = detalle, onCambio = { detalle = it }, placeholder = "", modifier = Modifier.fillMaxWidth())
            Text(
                "Tu correo institucional no se puede cambiar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BotonPrimario(
            onClick = {
                guardando = true
                alcance.launch {
                    try {
                        Api.actualizarPerfil(nombre, detalle, usuario.mostrarCorreo)
                        avisar("Perfil actualizado.")
                        onListo()
                    } catch (e: ErrorApi) {
                        avisar(e.message.orEmpty())
                    } finally {
                        guardando = false
                    }
                }
            },
            enabled = valido && !guardando,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (guardando) "Guardando…" else "Guardar cambios") }
    }
}
