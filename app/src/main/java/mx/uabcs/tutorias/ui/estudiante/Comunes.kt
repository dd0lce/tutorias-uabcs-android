@file:OptIn(ExperimentalLayoutApi::class)

package mx.uabcs.tutorias.ui.estudiante

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import mx.uabcs.tutorias.ui.BotonSecundario
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.Tutor
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.theme.TemaUabcs

private val ES = Locale.forLanguageTag("es-MX")
private fun limpiar(s: String) = s.replace(".", "").replaceFirstChar { it.titlecase(ES) }

fun fechaCorta(d: LocalDate): String = limpiar(d.format(DateTimeFormatter.ofPattern("EEE d MMM", ES)))
fun diaCorto(d: LocalDate): String = limpiar(d.format(DateTimeFormatter.ofPattern("EEE d", ES)))
fun fechaLarga(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES)).replaceFirstChar { it.titlecase(ES) }
fun hora(h: LocalDateTime): String = h.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
fun mesAnio(m: YearMonth): String = m.format(DateTimeFormatter.ofPattern("LLLL yyyy", ES)).replaceFirstChar { it.titlecase(ES) }

fun rango(inicio: LocalDateTime): String {
    val fin = inicio.plusHours(1)
    return if ((inicio.hour < 12) == (fin.hour < 12)) {
        "${inicio.format(DateTimeFormatter.ofPattern("h:mm"))}–${hora(fin)}"
    } else {
        "${hora(inicio)}–${hora(fin)}"
    }
}

fun fechaHora(inicio: LocalDateTime) = "${fechaCorta(inicio.toLocalDate())} · ${rango(inicio)}"

fun lugar(s: Solicitud, t: Tutor) =
    if (s.modalidad == Modalidad.PRESENCIAL) "Presencial · ${t.lugar}" else "En línea · Google Meet"

fun formatoTexto(s: Solicitud) =
    if (s.formato == Formato.GRUPAL) "Grupal (${s.companeros.size + 1})" else Formato.INDIVIDUAL.nombre

fun proximoHorario(t: Tutor): String =
    Tutorias.horariosLibres(t).firstOrNull()
        ?.let { "Próximo horario: ${fechaCorta(it.toLocalDate())}, ${hora(it)}" }
        ?: "Sin horarios libres en las próximas 2 semanas"

fun <T> Set<T>.alternar(x: T): Set<T> = if (x in this) this - x else this + x

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
fun TarjetaCita(s: Solicitud, onDetalle: () -> Unit, onMensajes: () -> Unit, conAcciones: Boolean = true) {
    val t = Tutorias.tutor(s.tutorId)
    Tarjeta(onClick = onDetalle) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(s.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            BadgeEstado(s.estado)
        }
        FilaIcono(Icons.Outlined.CalendarToday, s.inicio?.let(::fechaHora) ?: "Sin fecha · ${s.nota.orEmpty()}")
        FilaIcono(if (s.modalidad == Modalidad.PRESENCIAL) Icons.Outlined.Place else Icons.Outlined.Videocam, lugar(s, t))
        val detalle = if (s.estado == Estado.RECHAZADA || s.estado == Estado.CANCELADA) s.nota.orEmpty() else formatoTexto(s)
        FilaIcono(Icons.Outlined.Person, "Con ${t.nombre} · $detalle")
        if (conAcciones && s.activa) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonSecundario(onClick = onDetalle, shape = MaterialTheme.shapes.small) { Text("Ver detalle") }
                TextButton(onClick = onMensajes) { Text("Mensajes") }
            }
        }
    }
}

@Composable
fun TarjetaTutor(t: Tutor, onClick: () -> Unit) {
    Tarjeta(onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarIniciales(t.iniciales)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(t.nombre, style = MaterialTheme.typography.titleMedium)
                Text(t.resumen, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.StarOutline, null, Modifier.size(14.dp), tint = TemaUabcs.colores.advertencia)
                    Text("${t.calificacion} · ${t.sesiones} sesiones · ${t.area}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    (t.modalidades.map { it.nombre } + t.formatos.joinToString(" / ") { it.nombre }).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(proximoHorario(t), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Ver perfil de ${t.nombre}")
        }
    }
}

@Composable
fun OpcionTarjeta(
    titulo: String,
    descripcion: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
) {
    val esquema = MaterialTheme.colorScheme
    Surface(
        selected = seleccionada,
        onClick = onClick,
        enabled = habilitada,
        shape = MaterialTheme.shapes.medium,
        color = if (seleccionada) esquema.primaryContainer else esquema.surface,
        border = BorderStroke(if (seleccionada) 2.dp else 1.dp, if (seleccionada) esquema.primary else esquema.outlineVariant),
        modifier = modifier.fillMaxWidth().semantics { role = Role.RadioButton },
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = seleccionada, onClick = null, enabled = habilitada)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = if (habilitada) esquema.onSurface else esquema.outline)
                Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = esquema.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ChipHora(horario: LocalDateTime, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(hora(horario)) },
        leadingIcon = if (seleccionado) {
            { Icon(Icons.Outlined.Check, null, Modifier.size(18.dp)) }
        } else {
            null
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier.semantics { contentDescription = "${fechaCorta(horario.toLocalDate())}, ${hora(horario)}" },
    )
}

enum class Vista(val nombre: String, val icono: ImageVector) {
    SEMANA("Semana", Icons.Outlined.DateRange),
    MES("Mes", Icons.Outlined.CalendarMonth),
    LISTA("Lista", Icons.AutoMirrored.Outlined.FormatListBulleted),
}

@Composable
fun SelectorVista(vista: Vista, onVista: (Vista) -> Unit) {
    val esquema = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(10.dp), color = esquema.surfaceVariant) {
        Row(Modifier.padding(4.dp).selectableGroup()) {
            Vista.entries.forEach { v ->
                val sel = v == vista
                Surface(
                    selected = sel,
                    onClick = { onVista(v) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (sel) esquema.surface else Color.Transparent,
                    contentColor = if (sel) esquema.primary else esquema.onSurfaceVariant,
                    shadowElevation = if (sel) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f).semantics { role = Role.Tab },
                ) {
                    Row(
                        Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(v.icono, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(v.nombre, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarioMes(
    mes: YearMonth,
    onMes: (YearMonth) -> Unit,
    marcados: Set<LocalDate>,
    seleccionado: LocalDate?,
    onDia: (LocalDate) -> Unit,
    leyenda: String,
) {
    val esquema = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onMes(mes.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            Text(mesAnio(mes), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { onMes(mes.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }
        Row {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = esquema.onSurfaceVariant)
            }
        }
        val desfase = mes.atDay(1).dayOfWeek.value - 1
        val semanas = (desfase + mes.lengthOfMonth() + 6) / 7
        for (semana in 0 until semanas) {
            Row {
                for (col in 0 until 7) {
                    val n = semana * 7 + col - desfase + 1
                    Box(Modifier.weight(1f).aspectRatio(1.15f).padding(2.dp)) {
                        if (n in 1..mes.lengthOfMonth()) {
                            val dia = mes.atDay(n)
                            val marcado = dia in marcados
                            val sel = dia == seleccionado
                            Surface(
                                onClick = { onDia(dia) },
                                enabled = marcado,
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    sel -> esquema.primary
                                    marcado -> esquema.primaryContainer
                                    else -> Color.Transparent
                                },
                                contentColor = when {
                                    sel -> esquema.onPrimary
                                    marcado -> esquema.primary
                                    else -> esquema.onSurface
                                },
                                modifier = Modifier.fillMaxSize().semantics {
                                    contentDescription = fechaLarga(dia) + if (marcado) ", disponible" else ""
                                },
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("$n", style = MaterialTheme.typography.bodyMedium)
                                    if (marcado) Box(Modifier.size(4.dp).clip(CircleShape).background(LocalContentColor.current))
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(esquema.primary))
            Text(leyenda, style = MaterialTheme.typography.bodySmall, color = esquema.onSurfaceVariant)
        }
    }
}

@Composable
fun VistaHorarios(tutor: Tutor, seleccionado: LocalDateTime?, onElegir: (LocalDateTime) -> Unit) {
    var vista by rememberSaveable { mutableStateOf(Vista.SEMANA) }
    val libres = Tutorias.horariosLibres(tutor)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectorVista(vista) { vista = it }
        if (libres.isEmpty()) {
            Text("Sin horarios libres en las próximas 2 semanas.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }
        when (vista) {
            Vista.SEMANA -> HorarioSemana(tutor, libres, seleccionado, onElegir)
            Vista.MES -> HorarioMes(libres, seleccionado, onElegir)
            Vista.LISTA -> HorarioLista(libres, seleccionado, onElegir)
        }
    }
}

@Composable
private fun HorarioSemana(tutor: Tutor, libres: List<LocalDateTime>, seleccionado: LocalDateTime?, onElegir: (LocalDateTime) -> Unit) {
    val inicial = (seleccionado ?: libres.first()).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    var lunes by rememberSaveable { mutableStateOf(inicial) }
    val dias = (0L..5L).map { lunes.plusDays(it) }
    val horas = tutor.horarios.filter { it.toLocalDate() in dias }.map { it.hour }.distinct().sorted()
    val c = TemaUabcs.colores
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { lunes = lunes.minusWeeks(1) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Semana anterior")
            }
            val sabado = dias.last()
            val titulo = if (lunes.month == sabado.month) {
                "${lunes.dayOfMonth} – ${sabado.dayOfMonth} de ${sabado.format(DateTimeFormatter.ofPattern("MMMM yyyy", ES))}"
            } else {
                "${fechaCorta(lunes)} – ${fechaCorta(sabado)}"
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { lunes = lunes.plusWeeks(1) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Semana siguiente")
            }
        }
        Row {
            Spacer(Modifier.width(44.dp))
            dias.forEach {
                Text(diaCorto(it), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (horas.isEmpty()) {
            Text("Sin horarios esta semana.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        horas.forEach { h ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$h:00", Modifier.width(44.dp), style = MaterialTheme.typography.bodySmall)
                dias.forEach { d ->
                    val slot = d.atTime(h, 0)
                    Box(Modifier.weight(1f).height(36.dp).padding(2.dp), contentAlignment = Alignment.Center) {
                        when {
                            slot in libres -> {
                                val sel = slot == seleccionado
                                Surface(
                                    onClick = { onElegir(slot) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (sel) MaterialTheme.colorScheme.primary else c.exitoSuave,
                                    contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else c.exito,
                                    modifier = Modifier.fillMaxSize().semantics {
                                        contentDescription = "Libre, ${fechaCorta(d)}, ${hora(slot)}"
                                    },
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (sel) "✓" else "Libre", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            slot in tutor.horarios -> Surface(shape = RoundedCornerShape(6.dp), color = c.superficieAlt, modifier = Modifier.fillMaxSize()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Lleno", style = MaterialTheme.typography.labelSmall, color = c.textoSecundario)
                                }
                            }
                            else -> Text("—", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorarioMes(libres: List<LocalDateTime>, seleccionado: LocalDateTime?, onElegir: (LocalDateTime) -> Unit) {
    val primero = (seleccionado ?: libres.first()).toLocalDate()
    var mes by rememberSaveable { mutableStateOf(YearMonth.from(primero)) }
    var dia by rememberSaveable { mutableStateOf(primero) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CalendarioMes(mes, { mes = it }, libres.map { it.toLocalDate() }.toSet(), dia, { dia = it }, "Días con horarios disponibles")
        val delDia = libres.filter { it.toLocalDate() == dia }
        if (delDia.isNotEmpty()) {
            Text("${fechaCorta(dia)} · horarios libres", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                delDia.forEach { ChipHora(it, it == seleccionado) { onElegir(it) } }
            }
        }
    }
}

@Composable
private fun HorarioLista(libres: List<LocalDateTime>, seleccionado: LocalDateTime?, onElegir: (LocalDateTime) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        libres.groupBy { it.toLocalDate() }.forEach { (dia, horarios) ->
            Text(fechaCorta(dia), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                horarios.forEach { ChipHora(it, it == seleccionado) { onElegir(it) } }
            }
        }
    }
}
