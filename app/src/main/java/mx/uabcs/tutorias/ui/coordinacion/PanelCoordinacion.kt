package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.ResumenCoordinacion
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.estudiante.Pantalla
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaCorta
import mx.uabcs.tutorias.ui.theme.TemaUabcs
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun PanelCoordinacion(onVerContenido: (String) -> Unit, modifier: Modifier = Modifier) {
    val resumen = Tutorias.resumenCoordinacion()
    val recientes = Tutorias.autorizadas()

    Pantalla(modifier) {
        Kpis(resumen, completo = true)
        AvisoPrivacidad()
        Text(
            "Autorizadas recientemente (${recientes.size.coerceAtMost(RECIENTES)} de ${recientes.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (recientes.isEmpty()) SinResultados("Todavía no hay sesiones autorizadas por los estudiantes.")
        recientes.take(RECIENTES).forEach { TarjetaAutorizada(it, onVerContenido = { onVerContenido(it.id) }) }
    }
}

private const val RECIENTES = 2

/** Contadores del panel. Sin [completo] solo se muestran los tres primeros. */
@Composable
internal fun Kpis(r: ResumenCoordinacion, completo: Boolean) {
    val c = TemaUabcs.colores
    val filas = listOf(
        listOf(
            Triple("${r.sesionesMes}", "Sesiones este mes", c.primarioSuave to c.primario),
            Triple("${r.autorizadas}", "Autorizadas por el estudiante", c.exitoSuave to c.exito),
            Triple("${r.tutoresActivos}", "Tutores activos", c.infoSuave to c.info),
        ),
        listOf(
            Triple("${r.pendientes}", "Solicitudes pendientes", c.primarioSuave to c.primario),
            Triple("${r.cancelacionesMes}", "Cancelaciones este mes", c.exitoSuave to c.exito),
            Triple(r.asistencia?.let { "$it %" } ?: "—", "Asistencia promedio", c.infoSuave to c.info),
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        filas.take(if (completo) 2 else 1).forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { (numero, etiqueta, colores) -> TarjetaKpi(numero, etiqueta, colores.first, colores.second, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TarjetaKpi(numero: String, etiqueta: String, fondo: Color, colorTexto: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = fondo) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(numero, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = colorTexto)
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TemaUabcs.colores.texto, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun AvisoPrivacidad() {
    val c = TemaUabcs.colores
    Surface(shape = MaterialTheme.shapes.medium, color = c.infoSuave, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Shield, contentDescription = null, tint = c.info, modifier = Modifier.size(24.dp))
            Text(
                "Solo ves el contenido académico (acuerdos, pendientes y materiales) de las sesiones en las que el estudiante activó " +
                    "«Permitir que Coordinación de Tutorías vea el contenido académico». Las demás muestran solo datos generales.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.texto,
            )
        }
    }
}

@Composable
internal fun TarjetaAutorizada(s: Solicitud, onVerContenido: () -> Unit) {
    val c = TemaUabcs.colores
    val fecha = s.inicio?.let { fechaCorta(it.toLocalDate()) } ?: "Sin fecha"
    Tarjeta {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(s.materia, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Surface(shape = CircleShape, color = c.exitoSuave) {
                Text(
                    "Autorizada",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.exito,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Text(
            "${s.estudiante?.nombre ?: "Estudiante"} · con ${Tutorias.tutor(s.tutorId).nombre} · $fecha",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BotonSecundario(onClick = onVerContenido, modifier = Modifier.fillMaxWidth()) { Text("Ver contenido académico") }
    }
}

@Composable
internal fun SinResultados(texto: String) {
    Tarjeta { Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PanelCoordinacionPreview() {
    TutoriasTheme { PanelCoordinacion(onVerContenido = {}) }
}
