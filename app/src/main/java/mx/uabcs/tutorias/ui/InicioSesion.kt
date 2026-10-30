package mx.uabcs.tutorias.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Cuentas
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun InicioSesionScreen(onIngresar: (Usuario) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var verContrasena by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val pendiente = rememberPendiente()

    fun intentar() {
        if (!Cuentas.esInstitucional(correo)) {
            error = "Usa tu correo institucional (@uabcs.mx o @alu.uabcs.mx)."
            return
        }
        val usuario = Cuentas.iniciarSesion(correo, contrasena)
        if (usuario == null) error = "Correo o contraseña incorrectos." else onIngresar(usuario)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MarcaUabcs()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Inicia sesión", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Usa tu correo institucional UABCS y tu contraseña.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CampoTexto(
                    etiqueta = "Correo institucional",
                    valor = correo,
                    onCambio = { correo = it; error = null },
                    placeholder = "nombre@alu.uabcs.mx",
                    ayuda = "Ej. nombre@alu.uabcs.mx",
                    esError = error != null,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                CampoTexto(
                    etiqueta = "Contraseña",
                    valor = contrasena,
                    onCambio = { contrasena = it; error = null },
                    placeholder = "••••••••",
                    esError = error != null,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    acciones = KeyboardActions(onDone = { intentar() }),
                    transformacion = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    final = {
                        IconButton(onClick = { verContrasena = !verContrasena }) {
                            Icon(
                                if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (verContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                            )
                        }
                    },
                )
                error?.let {
                    Row(
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = pendiente) { Text("¿Olvidaste tu contraseña?") }
                Button(
                    onClick = { intentar() },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) { Text("Iniciar sesión") }
                ConexionSegura(Modifier.align(Alignment.CenterHorizontally))
                Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = pendiente) { Text("Regístrate con tu correo UABCS") }
                }
                PieLegal()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioSesionPreview() {
    TutoriasTheme { InicioSesionScreen(onIngresar = {}) }
}
