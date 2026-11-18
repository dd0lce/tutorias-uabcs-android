package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.estudiante.AvatarIniciales
import mx.uabcs.tutorias.ui.estudiante.Pantalla
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.rememberPendiente
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun PerfilCoordinacion(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendiente = rememberPendiente()
    // ponytail: solo sobreviven a girar la pantalla; guardarlos en el servidor cuando alguien muestre estos datos.
    var verCorreo by rememberSaveable { mutableStateOf(true) }
    var verFoto by rememberSaveable { mutableStateOf(true) }
    val iniciales = usuario.nombre.split(" ").filter { it.firstOrNull()?.isUpperCase() == true && !it.endsWith(".") }
        .take(2).joinToString("") { it.take(1) }.ifEmpty { "U" }

    Pantalla(modifier) {
        Tarjeta {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(iniciales, tamano = 72.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(usuario.detalle, style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Visibility, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Nombre y cargo siempre visibles",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            BotonSecundario(onClick = pendiente, modifier = Modifier.fillMaxWidth()) { Text("Editar perfil") }
        }

        Tarjeta {
            Text("Visibilidad de mis datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FilaVisibilidad("Correo institucional", usuario.correo, verCorreo) { verCorreo = it }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FilaVisibilidad("Foto de perfil", "Visible para tutores", verFoto) { verFoto = it }
        }

        Tarjeta(onClick = pendiente) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null)
                Column(Modifier.weight(1f)) {
                    Text("Mis datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Ver, descargar o eliminar tus datos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        BotonSecundario(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun FilaVisibilidad(titulo: String, subtitulo: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PerfilCoordinacionPreview() {
    TutoriasTheme {
        PerfilCoordinacion(Usuario("Mtra. Laura Peña", "lpena@uabcs.mx", Rol.COORDINACION, "Coordinación de Tutorías"), onCerrarSesion = {})
    }
}
