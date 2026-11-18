@file:OptIn(ExperimentalLayoutApi::class)

package mx.uabcs.tutorias.ui.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import mx.uabcs.tutorias.ui.Iconos
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Filtros
import mx.uabcs.tutorias.data.Franja
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.data.Usuario
import mx.uabcs.tutorias.ui.BotonPrimario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.theme.TemaUabcs
import mx.uabcs.tutorias.ui.Pantalla
import mx.uabcs.tutorias.ui.Tarjeta
import mx.uabcs.tutorias.ui.AvisosInstitucionales
import mx.uabcs.tutorias.ui.Encabezado

@Composable
fun InicioEstudiante(
    usuario: Usuario,
    onBuscar: (String) -> Unit,
    onVerSolicitudes: (calendario: Boolean) -> Unit,
    onDetalle: (String) -> Unit,
    onMensajes: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var texto by rememberSaveable { mutableStateOf("") }
    val c = TemaUabcs.colores
    Pantalla(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("¡Hola, ${usuario.nombre.substringBefore(' ')}!", style = MaterialTheme.typography.headlineLarge)
            Text("¿En qué te ayudamos hoy?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Tarjeta {
            CampoTexto(
                etiqueta = "Buscar tutor o asesor",
                valor = texto,
                onCambio = { texto = it },
                placeholder = "Materia, tutor o tema",
                icono = Iconos.buscar,
                teclado = KeyboardOptions(imeAction = ImeAction.Search),
                acciones = KeyboardActions(onSearch = { onBuscar(texto) }),
            )
            BotonPrimario(onClick = { onBuscar(texto) }, modifier = Modifier.fillMaxWidth()) {
                Text("Buscar tutor")
            }
        }
        Encabezado("Mis solicitudes", "Ver todas") { onVerSolicitudes(false) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador(Tutorias.cuantas(Estado.PENDIENTE), "Pendientes", c.advertenciaSuave, c.advertencia, Modifier.weight(1f))
            Contador(Tutorias.cuantas(Estado.CONFIRMADA), "Confirmadas", c.exitoSuave, c.exito, Modifier.weight(1f))
            Contador(Tutorias.cuantas(Estado.RECHAZADA), "Rechazadas", c.peligroSuave, c.peligro, Modifier.weight(1f))
        }
        Encabezado("Próximas citas", "Ver calendario") { onVerSolicitudes(true) }
        val proximas = Tutorias.proximas().take(3)
        if (proximas.isEmpty()) {
            Text("No tienes citas próximas. Busca un tutor para solicitar una sesión.", style = MaterialTheme.typography.bodyMedium)
        }
        proximas.forEach { TarjetaCita(it, onDetalle = { onDetalle(it.id) }, onMensajes = { onMensajes(it.id) }) }
        AvisosInstitucionales()
    }
}

@Composable
private fun Contador(n: Int, etiqueta: String, fondo: Color, color: Color, modifier: Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = fondo) {
        Column(Modifier.padding(12.dp)) {
            Text("$n", style = MaterialTheme.typography.headlineLarge, color = color)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun BuscarTutor(textoInicial: String, onTutor: (String) -> Unit, modifier: Modifier = Modifier) {
    var texto by rememberSaveable(textoInicial) { mutableStateOf(textoInicial) }
    var modalidades by rememberSaveable { mutableStateOf(emptySet<Modalidad>()) }
    var dias by rememberSaveable { mutableStateOf(emptySet<DayOfWeek>()) }
    var franjas by rememberSaveable { mutableStateOf(emptySet<Franja>()) }
    var verFiltros by rememberSaveable { mutableStateOf(false) }
    val filtros = Filtros(texto, modalidades, dias, franjas)
    val resultados = Tutorias.buscar(filtros)
    val foco = LocalFocusManager.current

    Pantalla(modifier, espacio = 12.dp) {
        CampoTexto(
            etiqueta = "Buscar por materia, tema o nombre del tutor",
            valor = texto,
            onCambio = { texto = it },
            placeholder = "Ej. Cálculo",
            icono = Iconos.buscar,
            teclado = KeyboardOptions(imeAction = ImeAction.Search),
            acciones = KeyboardActions(onSearch = { foco.clearFocus() }),
        )
        FilterChip(
            selected = filtros.activos > 0,
            onClick = { verFiltros = !verFiltros },
            label = { Text(if (filtros.activos > 0) "Filtros (${filtros.activos})" else "Filtros") },
            leadingIcon = { Icon(Iconos.filtro, contentDescription = null) },
        )
        AnimatedVisibility(verFiltros) {
            Tarjeta {
                GrupoFiltro("Modalidad") {
                    Modalidad.entries.forEach { m ->
                        FilterChip(m in modalidades, { modalidades = modalidades.alternar(m) }, { Text(m.nombre) })
                    }
                }
                GrupoFiltro("Día") {
                    DayOfWeek.entries.take(6).forEach { d ->
                        FilterChip(d in dias, { dias = dias.alternar(d) }, { Text(nombreDia(d)) })
                    }
                }
                GrupoFiltro("Horario") {
                    Franja.entries.forEach { f ->
                        FilterChip(f in franjas, { franjas = franjas.alternar(f) }, { Text(f.nombre) })
                    }
                }
                TextButton(onClick = { modalidades = emptySet(); dias = emptySet(); franjas = emptySet() }) { Text("Limpiar") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (resultados.size == 1) "1 tutor encontrado" else "${resultados.size} tutores encontrados",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
            )
            Text("Orden: disponibilidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (resultados.isEmpty()) {
            Text("Nadie coincide con esa búsqueda. Prueba con otra materia o quita filtros.", style = MaterialTheme.typography.bodyMedium)
        }
        resultados.forEach { t -> TarjetaTutor(t) { onTutor(t.id) } }
    }
}

@Composable
private fun GrupoFiltro(titulo: String, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}
