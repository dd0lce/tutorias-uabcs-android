package mx.uabcs.tutorias.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.R
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.theme.TemaUabcs

@Composable
fun MarcaUabcs(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.logo_uabcs), "Logotipo de la UABCS", Modifier.size(44.dp))
        Column {
            Text("Tutorías UABCS", style = MaterialTheme.typography.titleMedium)
            Text(
                "Universidad Autónoma de Baja California Sur",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    esError: Boolean = false,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    acciones: KeyboardActions = KeyboardActions.Default,
    transformacion: VisualTransformation = VisualTransformation.None,
    icono: ImageVector? = null,
    minLineas: Int = 1,
    final: @Composable (() -> Unit)? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder) },
            singleLine = minLineas == 1,
            minLines = minLineas,
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            isError = esError,
            keyboardOptions = teclado,
            keyboardActions = acciones,
            visualTransformation = transformacion,
            trailingIcon = final,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TemaUabcs.colores.foco,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiqueta },
        )
        if (ayuda != null) {
            Text(
                ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun BotonPrimario(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        content = content,
    )
}

@Composable
fun BotonSecundario(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        content = content,
    )
}

@Composable
fun PieLegal(modifier: Modifier = Modifier) {
    var verPrivacidad by rememberSaveable { mutableStateOf(false) }
    if (verPrivacidad) AvisoPrivacidadDialogo { verPrivacidad = false }
    val gris = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Iconos.escudo, contentDescription = null, tint = gris, modifier = Modifier.size(14.dp))
        TextButton(onClick = { verPrivacidad = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
            Text(
                "Aviso de privacidad",
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
            )
        }
        Text("·  © 2026 UABCS · Tutorías", style = MaterialTheme.typography.bodySmall, color = gris)
    }
}

@Composable
fun Pantalla(
    modifier: Modifier = Modifier,
    espacio: Dp = 16.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(espacio),
        content = contenido,
    )
}

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val borde = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val cuerpo: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (titulo != null) Text(titulo, style = MaterialTheme.typography.headlineSmall)
            contenido()
        }
    }
    if (onClick == null) {
        Card(modifier.fillMaxWidth(), colors = colores, border = borde, content = cuerpo)
    } else {
        Card(onClick, modifier.fillMaxWidth(), colors = colores, border = borde, content = cuerpo)
    }
}

@Composable
fun AvatarIniciales(iniciales: String, modifier: Modifier = Modifier, tamano: Dp = 48.dp) {
    Box(
        modifier.size(tamano).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            iniciales,
            style = if (tamano >= 64.dp) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BadgeEstado(estado: Estado) {
    val c = TemaUabcs.colores
    val (fondo, texto) = when (estado) {
        Estado.PENDIENTE -> c.advertenciaSuave to c.advertencia
        Estado.CONFIRMADA -> c.exitoSuave to c.exito
        Estado.RECHAZADA -> c.peligroSuave to c.peligro
        Estado.CANCELADA -> c.superficieAlt to c.textoSecundario
        Estado.LISTA_ESPERA -> c.esperaSuave to c.espera
        Estado.COMPLETADA -> c.infoSuave to c.info
    }
    Surface(shape = CircleShape, color = fondo) {
        Text(
            estado.nombre,
            Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = texto,
        )
    }
}

@Composable
fun FilaIcono(icono: ImageVector, texto: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DatoFila(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun Encabezado(titulo: String, accion: String, onAccion: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        TextButton(onClick = onAccion) { Text(accion) }
    }
}

@Composable
fun Fila(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icono, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Iconos.siguiente, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AvisosInstitucionales() {
    val avisos = Tutorias.avisosVigentes()
    if (avisos.isEmpty()) return
    Tarjeta(titulo = "Avisos") {
        avisos.forEachIndexed { i, a ->
            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Iconos.info, contentDescription = null, tint = TemaUabcs.colores.info)
                Column {
                    Text(a.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(a.detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
