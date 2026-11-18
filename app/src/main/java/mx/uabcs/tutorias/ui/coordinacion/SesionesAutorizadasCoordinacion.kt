package mx.uabcs.tutorias.ui.coordinacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.estudiante.BadgeEstado
import mx.uabcs.tutorias.ui.estudiante.DatoFila
import mx.uabcs.tutorias.ui.estudiante.Pantalla
import mx.uabcs.tutorias.ui.estudiante.Tarjeta
import mx.uabcs.tutorias.ui.estudiante.fechaHora
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

@Composable
fun SesionesAutorizadasCoordinacion(onVerContenido: (String) -> Unit, modifier: Modifier = Modifier) {
    var texto by rememberSaveable { mutableStateOf("") }
    val sesiones = Tutorias.autorizadas(texto)
    val total = Tutorias.autorizadas().size

    Pantalla(modifier) {
        Kpis(Tutorias.resumenCoordinacion(), completo = false)
        AvisoPrivacidad()
        CampoTexto(
            etiqueta = "Buscar",
            valor = texto,
            onCambio = { texto = it },
            placeholder = "Estudiante, tutor o materia",
            icono = Icons.Outlined.Search,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Sesiones autorizadas (${sesiones.size} de $total)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (sesiones.isEmpty()) {
            SinResultados(if (total == 0) "Todavía no hay sesiones autorizadas por los estudiantes." else "No se encontraron sesiones que coincidan con la búsqueda.")
        }
        sesiones.forEach { TarjetaAutorizada(it, onVerContenido = { onVerContenido(it.id) }) }
    }
}

/** Lo que Coordinación puede ver de una sesión, solo si el estudiante lo autorizó. */
@Composable
fun ContenidoAcademico(id: String, modifier: Modifier = Modifier) {
    val s = Tutorias.solicitud(id) ?: return
    Pantalla(modifier) {
        if (!s.coordinacionPuedeVer) {
            SinResultados("El estudiante no autorizó que Coordinación vea el contenido de esta sesión.")
            return@Pantalla
        }
        Tarjeta {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                BadgeEstado(s.estado)
            }
            DatoFila("Estudiante", s.estudiante?.let { "${it.nombre} · ${it.detalle}" } ?: "Sin datos")
            DatoFila("Tutor", Tutorias.tutor(s.tutorId).nombre)
            DatoFila("Fecha y hora", s.inicio?.let(::fechaHora) ?: "Sin fecha")
            DatoFila("Tema", s.tema)
            DatoFila("Objetivo", s.objetivo)
            s.archivo?.let { DatoFila("Archivo", it) }
        }
        Tarjeta(titulo = "Registro de la sesión") {
            if (s.estado == Estado.COMPLETADA) {
                DatoFila("Asistencia", if (s.asistio == true) "Asistió" else "No asistió")
                DatoFila("Acuerdos", s.notasSesion ?: "Sin acuerdos registrados")
            } else {
                Text("El tutor todavía no registra esta sesión.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SesionesAutorizadasCoordinacionPreview() {
    TutoriasTheme { SesionesAutorizadasCoordinacion(onVerContenido = {}) }
}
