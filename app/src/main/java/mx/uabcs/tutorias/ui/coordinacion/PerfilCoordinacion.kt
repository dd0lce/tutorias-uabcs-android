package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Cuentas
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.rememberPendiente
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun PerfilCoordinacion(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendiente = rememberPendiente()
    var visibilidadCorreo by remember { mutableStateOf(true) }
    var visibilidadTelefono by remember { mutableStateOf(false) }
    var visibilidadFoto by remember { mutableStateOf(true) }

    val iniciales = remember(usuario.nombre) {
        usuario.nombre
            .replace("Mtra.", "")
            .replace("Mtro.", "")
            .trim()
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "U" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Tarjeta 1: Información del Usuario (Foto, Nombre, Cargo, Botón Editar)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Círculo del Avatar con iniciales ("LP")
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = iniciales,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    // Nombre y Cargo
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = usuario.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = usuario.detalle,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Departamento Académico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Nombre y cargo siempre visibles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Botón Editar perfil
                BotonSecundario(
                    onClick = pendiente,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Editar perfil")
                }
            }
        }

        // Tarjeta 2: Visibilidad de mis datos
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Visibilidad de mis datos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Fila 1: Correo institucional
                FilaVisibilidad(
                    titulo = "Correo institucional",
                    subtitulo = usuario.correo,
                    checked = visibilidadCorreo,
                    onCheckedChange = { visibilidadCorreo = it },
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )

                // Fila 2: Teléfono
                FilaVisibilidad(
                    titulo = "Teléfono",
                    subtitulo = "612 ••• •• 31",
                    checked = visibilidadTelefono,
                    onCheckedChange = { visibilidadTelefono = it },
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )

                // Fila 3: Foto de perfil
                FilaVisibilidad(
                    titulo = "Foto de perfil",
                    subtitulo = "Visible para tutores",
                    checked = visibilidadFoto,
                    onCheckedChange = { visibilidadFoto = it },
                )
            }
        }

        // Tarjeta 3: Mis datos (escudo)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Column {
                            Text(
                                text = "Mis datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Ver, descargar o eliminar tus datos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Botón Cerrar sesión
        BotonSecundario(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun FilaVisibilidad(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PerfilCoordinacionPreview() {
    TutoriasTheme {
        PerfilCoordinacion(
            usuario = Cuentas.buscar("lpena@uabcs.mx")!!,
            onCerrarSesion = {},
        )
    }
}
