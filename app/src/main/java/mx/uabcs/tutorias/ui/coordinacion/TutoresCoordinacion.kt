package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.data.Filtros
import mx.uabcs.tutorias.data.Tutor
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.AvatarIniciales
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.theme.TemaUabcs
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun TutoresCoordinacion(onTutor: (String) -> Unit, modifier: Modifier = Modifier) {
    var texto by rememberSaveable { mutableStateOf("") }
    // Sin remember: Tutorias.buscar lee las listas de estado, así que se recalcula cuando un tutor cambia sus datos.
    val tutores = Tutorias.buscar(Filtros(texto))

    Pantalla(modifier) {
        CampoTexto(
            etiqueta = "Buscar tutor",
            valor = texto,
            onCambio = { texto = it },
            placeholder = "Nombre, materia o carrera",
            icono = Iconos.buscar,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Tutores (${tutores.size} de ${Tutorias.tutores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (tutores.isEmpty()) SinResultados("No se encontraron tutores que coincidan con la búsqueda.")
        tutores.forEach { TarjetaTutorCoordinacion(it, onClick = { onTutor(it.id) }) }
    }
}

@Composable
private fun TarjetaTutorCoordinacion(t: Tutor, onClick: () -> Unit) {
    val (sesiones, pendientes) = Tutorias.estadisticasTutor(t.id)
    Tarjeta(onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarIniciales(t.iniciales, tamano = 56.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(t.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(t.resumen, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Iconos.estrella, null, Modifier.size(16.dp), tint = TemaUabcs.colores.advertencia)
                    Text(
                        "${t.calificacion} · ${t.sesiones} sesiones · ${t.area}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    (t.modalidades.map { it.nombre } + t.formatos.joinToString(" / ") { it.nombre }).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Este mes: $sesiones ${if (sesiones == 1) "sesión" else "sesiones"} · " +
                        when (pendientes) { 0 -> "sin pendientes"; 1 -> "1 pendiente"; else -> "$pendientes pendientes" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Iconos.siguiente, contentDescription = "Ver perfil de ${t.nombre}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TutoresCoordinacionPreview() {
    TutoriasTheme { TutoresCoordinacion(onTutor = {}) }
}
