package mx.uabcs.tutorias.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.R
import mx.uabcs.tutorias.ui.theme.TemaUabcs

@Composable
fun rememberPendiente(): () -> Unit {
    val contexto = LocalContext.current
    return remember(contexto) {
        { Toast.makeText(contexto, "Disponible en una próxima versión.", Toast.LENGTH_SHORT).show() }
    }
}

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
fun BotonSecundario(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        content = content,
    )
}

@Composable
fun PieLegal(modifier: Modifier = Modifier) {
    val pendiente = rememberPendiente()
    val gris = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Shield, contentDescription = null, tint = gris, modifier = Modifier.size(14.dp))
        TextButton(onClick = pendiente, contentPadding = PaddingValues(horizontal = 6.dp)) {
            Text(
                "Aviso de privacidad",
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
            )
        }
        Text("·  © 2026 UABCS · Tutorías", style = MaterialTheme.typography.bodySmall, color = gris)
    }
}
