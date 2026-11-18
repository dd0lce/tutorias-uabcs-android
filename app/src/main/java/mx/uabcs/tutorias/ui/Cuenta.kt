package mx.uabcs.tutorias.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.uabcs.tutorias.data.Api
import mx.uabcs.tutorias.data.ErrorApi
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

private const val MIN_CONTRASENA = 8

/** Registro de estudiantes: primero se verifica el correo con un código y luego se crea la cuenta. */
@Composable
fun Registro(onVolver: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var codigoEnviado by rememberSaveable { mutableStateOf(false) }
    var codigo by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var detalle by rememberSaveable { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val accion = rememberAccion()

    PantallaCuenta(
        titulo = "Crea tu cuenta",
        subtitulo = if (!codigoEnviado) {
            "Solo para estudiantes con correo @alu.uabcs.mx. Te mandaremos un código para verificarlo."
        } else {
            "Escribe el código que llegó a $correo y completa tus datos."
        },
        error = accion.error,
        onVolver = onVolver,
    ) {
        if (!codigoEnviado) {
            CampoCorreo(correo) { correo = it; accion.error = null }
            BotonPrimario(
                onClick = {
                    if (!correo.trim().lowercase().endsWith("@alu.uabcs.mx")) {
                        accion.error = "Usa tu correo de estudiante (@alu.uabcs.mx)."
                    } else {
                        accion.correr { Api.pedirCodigoRegistro(correo); codigoEnviado = true }
                    }
                },
                enabled = !accion.cargando,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enviar código") }
        } else {
            CampoCodigo(codigo) { codigo = it; accion.error = null }
            CampoTexto("Nombre completo", nombre, { nombre = it; accion.error = null }, "Nombre y apellidos")
            CampoTexto(
                "Carrera y semestre", detalle, { detalle = it }, "Ej. Ing. en Desarrollo de Software · 3.er semestre",
            )
            CampoContrasena(contrasena) { contrasena = it; accion.error = null }
            BotonPrimario(
                onClick = {
                    when {
                        nombre.trim().length < 3 -> accion.error = "Escribe tu nombre completo."
                        contrasena.length < MIN_CONTRASENA -> accion.error = "La contraseña debe tener al menos $MIN_CONTRASENA caracteres."
                        // Al registrarse inicia sesión; TutoriasApp cambia de pantalla sola.
                        else -> accion.correr { Api.registrarse(correo, codigo, nombre, detalle, contrasena) }
                    }
                },
                enabled = !accion.cargando,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Crear cuenta") }
            TextButton(onClick = { codigoEnviado = false; codigo = "" }) { Text("Cambiar correo o pedir otro código") }
        }
    }
}

@Composable
fun RecuperarContrasena(onListo: () -> Unit, onVolver: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var codigoEnviado by rememberSaveable { mutableStateOf(false) }
    var codigo by rememberSaveable { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val accion = rememberAccion()

    PantallaCuenta(
        titulo = "Recupera tu contraseña",
        subtitulo = if (!codigoEnviado) {
            "Escribe tu correo institucional. Si tiene cuenta, te mandaremos un código."
        } else {
            "Si $correo tiene cuenta, ya le llegó un código. Escríbelo y elige una contraseña nueva."
        },
        error = accion.error,
        onVolver = onVolver,
    ) {
        if (!codigoEnviado) {
            CampoCorreo(correo) { correo = it; accion.error = null }
            BotonPrimario(
                onClick = { accion.correr { Api.pedirCodigoRecuperacion(correo); codigoEnviado = true } },
                enabled = !accion.cargando && correo.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enviar código") }
        } else {
            CampoCodigo(codigo) { codigo = it; accion.error = null }
            CampoContrasena(contrasena, etiqueta = "Contraseña nueva") { contrasena = it; accion.error = null }
            BotonPrimario(
                onClick = {
                    if (contrasena.length < MIN_CONTRASENA) {
                        accion.error = "La contraseña debe tener al menos $MIN_CONTRASENA caracteres."
                    } else {
                        accion.correr { Api.recuperar(correo, codigo, contrasena); onListo() }
                    }
                },
                enabled = !accion.cargando,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Guardar contraseña") }
            TextButton(onClick = { codigoEnviado = false; codigo = "" }) { Text("Pedir otro código") }
        }
    }
}

private class Accion(private val correrEn: (suspend () -> Unit) -> Unit) {
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun correr(bloque: suspend () -> Unit) {
        if (cargando) return
        cargando = true
        error = null
        correrEn {
            try {
                bloque()
            } catch (e: ErrorApi) {
                error = e.message
            } finally {
                cargando = false
            }
        }
    }
}

@Composable
private fun rememberAccion(): Accion {
    val alcance = rememberCoroutineScope()
    return remember { Accion { bloque -> alcance.launch { bloque() } } }
}

@Composable
private fun PantallaCuenta(
    titulo: String,
    subtitulo: String,
    error: String?,
    onVolver: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MarcaUabcs()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(titulo, style = MaterialTheme.typography.headlineLarge)
                    Text(subtitulo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                contenido()
                error?.let {
                    Row(
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Iconos.alerta, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Volver a iniciar sesión")
                }
                PieLegal()
            }
        }
    }
}

@Composable
private fun CampoCorreo(valor: String, onCambio: (String) -> Unit) = CampoTexto(
    etiqueta = "Correo institucional",
    valor = valor,
    onCambio = onCambio,
    placeholder = "nombre@alu.uabcs.mx",
    teclado = KeyboardOptions(keyboardType = KeyboardType.Email),
)

@Composable
private fun CampoCodigo(valor: String, onCambio: (String) -> Unit) = CampoTexto(
    etiqueta = "Código de verificación",
    valor = valor,
    onCambio = { onCambio(it.filter(Char::isDigit).take(6)) },
    placeholder = "6 dígitos",
    ayuda = "Vence en 15 minutos.",
    teclado = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
)

@Composable
private fun CampoContrasena(valor: String, etiqueta: String = "Contraseña", onCambio: (String) -> Unit) = CampoTexto(
    etiqueta = etiqueta,
    valor = valor,
    onCambio = onCambio,
    placeholder = "••••••••",
    ayuda = "Al menos $MIN_CONTRASENA caracteres.",
    teclado = KeyboardOptions(keyboardType = KeyboardType.Password),
    transformacion = PasswordVisualTransformation(),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegistroPreview() {
    TutoriasTheme { Registro(onVolver = {}) }
}
