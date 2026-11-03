package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.PieLegal
import mx.uabcs.tutorias.ui.rememberPendiente
import mx.uabcs.tutorias.ui.theme.TemaUabcs
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun PanelCoordinacion(
    modifier: Modifier = Modifier,
) {
    val pendiente = rememberPendiente()
    val c = TemaUabcs.colores

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Grid de 6 Tarjetas de Contadores KPIs (2 filas de 3 columnas)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fila 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TarjetaKpi(
                    numero = "128",
                    etiqueta = "Sesiones\neste mes",
                    fondo = c.primarioSuave,
                    colorTexto = c.primario,
                    modifier = Modifier.weight(1f),
                )
                TarjetaKpi(
                    numero = "37",
                    etiqueta = "Autorizadas\npor el\nestudiante",
                    fondo = c.exitoSuave,
                    colorTexto = c.exito,
                    modifier = Modifier.weight(1f),
                )
                TarjetaKpi(
                    numero = "24",
                    etiqueta = "Tutores\nactivos",
                    fondo = c.infoSuave,
                    colorTexto = c.info,
                    modifier = Modifier.weight(1f),
                )
            }

            // Fila 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TarjetaKpi(
                    numero = "14",
                    etiqueta = "Solicitudes\npendientes",
                    fondo = c.primarioSuave,
                    colorTexto = c.primario,
                    modifier = Modifier.weight(1f),
                )
                TarjetaKpi(
                    numero = "9",
                    etiqueta = "Cancelacio\nnes este\nmes",
                    fondo = c.exitoSuave,
                    colorTexto = c.exito,
                    modifier = Modifier.weight(1f),
                )
                TarjetaKpi(
                    numero = "87 %",
                    etiqueta = "Asistencia\npromedio",
                    fondo = c.infoSuave,
                    colorTexto = c.info,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Tarjeta Informativa (Shield)
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = c.infoSuave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = c.info,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Solo ves el contenido académico (acuerdos, pendientes y materiales) de las sesiones en las que el estudiante activó «Permitir que Coordinación de Tutorías vea el contenido académico». Las demás muestran solo datos generales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.texto,
                )
            }
        }

        // Título de Sección
        Text(
            text = "Autorizadas recientemente (2 de 37)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        // Tarjeta de Sesión Autorizada 1
        TarjetaAutorizada(
            materia = "Cálculo diferencial",
            detalle = "Derrick Ramírez · con Mariana Cota · 24 sep 2026",
            onVerContenido = pendiente,
        )

        // Tarjeta de Sesión Autorizada 2
        TarjetaAutorizada(
            materia = "Estadística descriptiva",
            detalle = "Ana Martínez · con Sofía Amador · 22 sep 2026",
            onVerContenido = pendiente,
        )

        // Pie Legal
        PieLegal()
    }
}

@Composable
private fun TarjetaKpi(
    numero: String,
    etiqueta: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = fondo,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colorTexto,
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = TemaUabcs.colores.texto,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun TarjetaAutorizada(
    materia: String,
    detalle: String,
    onVerContenido: () -> Unit,
) {
    val c = TemaUabcs.colores

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = materia,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                // Insignia "Autorizada"
                Surface(
                    shape = CircleShape,
                    color = c.exitoSuave,
                ) {
                    Text(
                        text = "Autorizada",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.exito,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            BotonSecundario(
                onClick = onVerContenido,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ver contenido académico")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PanelCoordinacionPreview() {
    TutoriasTheme {
        PanelCoordinacion()
    }
}
