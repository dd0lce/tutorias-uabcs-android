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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.rememberPendiente
import mx.uabcs.tutorias.ui.theme.TemaUabcs
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

data class SesionAutorizada(
    val id: String,
    val materia: String,
    val estudiante: String,
    val tutor: String,
    val fecha: String,
)

private val sesionesMock = listOf(
    SesionAutorizada("1", "Cálculo diferencial", "Derrick Ramírez", "Mariana Cota", "24 sep 2026"),
    SesionAutorizada("2", "Estadística descriptiva", "Ana Martínez", "Sofía Amador", "22 sep 2026"),
    SesionAutorizada("3", "Programación I", "Carlos López", "Luis Geraldo", "20 sep 2026"),
    SesionAutorizada("4", "Física I", "María Gómez", "Rubén Castro Higuera", "18 sep 2026"),
)

@Composable
fun SesionesAutorizadasCoordinacion(
    modifier: Modifier = Modifier,
) {
    val pendiente = rememberPendiente()
    val c = TemaUabcs.colores
    var textoBusqueda by remember { mutableStateOf("") }

    val sesionesFiltradas = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            sesionesMock
        } else {
            val q = textoBusqueda.trim().lowercase()
            sesionesMock.filter {
                it.materia.lowercase().contains(q) ||
                    it.estudiante.lowercase().contains(q) ||
                    it.tutor.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Fila superior de 3 tarjetas KPI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TarjetaKpiSesiones(
                numero = "128",
                etiqueta = "Sesiones\neste mes",
                fondo = c.primarioSuave,
                colorTexto = c.primario,
                modifier = Modifier.weight(1f),
            )
            TarjetaKpiSesiones(
                numero = "37",
                etiqueta = "Autorizadas\npor el\nestudiante",
                fondo = c.exitoSuave,
                colorTexto = c.exito,
                modifier = Modifier.weight(1f),
            )
            TarjetaKpiSesiones(
                numero = "24",
                etiqueta = "Tutores\nactivos",
                fondo = c.infoSuave,
                colorTexto = c.info,
                modifier = Modifier.weight(1f),
            )
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

        // Campo de Búsqueda funcional
        CampoTexto(
            etiqueta = "Buscar",
            valor = textoBusqueda,
            onCambio = { textoBusqueda = it },
            placeholder = "Estudiante, tutor o materia",
            icono = Icons.Outlined.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        // Título de Sección con contador
        Text(
            text = "Sesiones autorizadas (${sesionesFiltradas.size} de 37)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        // Lista de sesiones
        if (sesionesFiltradas.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "No se encontraron sesiones que coincidan con la búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            sesionesFiltradas.forEach { sesion ->
                TarjetaSesionAutorizada(
                    sesion = sesion,
                    onVerContenido = pendiente,
                )
            }
        }
    }
}

@Composable
private fun TarjetaKpiSesiones(
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
private fun TarjetaSesionAutorizada(
    sesion: SesionAutorizada,
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
                    text = sesion.materia,
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
                text = "${sesion.estudiante} · con ${sesion.tutor} · ${sesion.fecha}",
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
private fun SesionesAutorizadasCoordinacionPreview() {
    TutoriasTheme {
        SesionesAutorizadasCoordinacion()
    }
}
