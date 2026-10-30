@file:OptIn(ExperimentalLayoutApi::class)

package mx.uabcs.tutorias.ui.estudiante

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import mx.uabcs.tutorias.ui.BotonSecundario
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.ZoneId
import mx.uabcs.tutorias.data.Cuentas
import mx.uabcs.tutorias.data.Estado
import mx.uabcs.tutorias.data.Formato
import mx.uabcs.tutorias.data.Modalidad
import mx.uabcs.tutorias.data.Solicitud
import mx.uabcs.tutorias.data.TipoSesion
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.theme.TemaUabcs

private val TITULOS_PASO = listOf("Tipo de sesión", "Modalidad", "Detalles", "Horario y confirmación")
private const val MAX_BYTES = 10L * 1024 * 1024

@Composable
fun NuevaSolicitud(
    tutorId: String,
    horarioInicial: LocalDateTime?,
    onEnviada: (String) -> Unit,
    onCancelar: () -> Unit,
    onOtroTutor: (String) -> Unit,
    avisar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = Tutorias.tutor(tutorId)
    val contexto = LocalContext.current
    var paso by rememberSaveable { mutableIntStateOf(1) }
    var tipo by rememberSaveable { mutableStateOf<TipoSesion?>(null) }
    var modalidad by rememberSaveable { mutableStateOf(t.modalidades.singleOrNull()) }
    var formato by rememberSaveable { mutableStateOf(t.formatos.singleOrNull()) }
    var correos by rememberSaveable { mutableStateOf("") }
    var materia by rememberSaveable { mutableStateOf(t.materias.first()) }
    var tema by rememberSaveable { mutableStateOf("") }
    var objetivo by rememberSaveable { mutableStateOf("") }
    var archivo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorArchivo by rememberSaveable { mutableStateOf<String?>(null) }
    var horario by rememberSaveable { mutableStateOf(horarioInicial) }

    val listaCorreos = correos.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    val correosValidos = listaCorreos.size in 1..4 && listaCorreos.all(Cuentas::esInstitucional)
    val libres = Tutorias.horariosLibres(t)
    val puedeSeguir = when (paso) {
        1 -> tipo != null
        2 -> modalidad != null && formato != null && (formato == Formato.INDIVIDUAL || correosValidos)
        3 -> tema.isNotBlank() && objetivo.trim().length >= 20
        else -> horario != null
    }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        contexto.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (!c.moveToFirst()) return@use
            if (c.getLong(1) > MAX_BYTES) {
                archivo = null
                errorArchivo = "El archivo pesa más de 10 MB."
            } else {
                archivo = c.getString(0)
                errorArchivo = null
            }
        }
    }

    fun enviar(inicio: LocalDateTime?) {
        val borrador = Solicitud(
            "", t.id, tipo!!, modalidad!!, formato!!, if (formato == Formato.GRUPAL) listaCorreos else emptyList(),
            materia, tema.trim(), objetivo.trim(), archivo, inicio, Estado.PENDIENTE,
        )
        try {
            onEnviada(Tutorias.enviar(borrador).id)
        } catch (e: IllegalArgumentException) {
            horario = null
            avisar("Ese horario ya no está libre. Elige otro.")
        }
    }

    BackHandler(enabled = paso > 1) { paso-- }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarIniciales(t.iniciales, tamano = 32.dp)
            Text("Solicitud con ${t.nombre} · ${t.resumen.substringBefore(" ·")}", style = MaterialTheme.typography.bodyMedium)
        }
        Progreso(paso)

        when (paso) {
            1 -> {
                Text("¿Qué tipo de sesión necesitas?", style = MaterialTheme.typography.headlineSmall)
                TipoSesion.entries.forEach { op ->
                    OpcionTarjeta(op.nombre, op.descripcion, tipo == op, { tipo = op })
                }
            }
            2 -> {
                Text("¿Cómo quieres tomar la sesión?", style = MaterialTheme.typography.headlineSmall)
                Text("Modalidad", style = MaterialTheme.typography.labelMedium)
                OpcionTarjeta(
                    "Presencial",
                    if (Modalidad.PRESENCIAL in t.modalidades) "${t.lugar}." else "No disponible con este tutor.",
                    modalidad == Modalidad.PRESENCIAL, { modalidad = Modalidad.PRESENCIAL },
                    habilitada = Modalidad.PRESENCIAL in t.modalidades,
                )
                OpcionTarjeta(
                    "En línea",
                    if (Modalidad.EN_LINEA in t.modalidades) "Google Meet institucional." else "No disponible con este tutor.",
                    modalidad == Modalidad.EN_LINEA, { modalidad = Modalidad.EN_LINEA },
                    habilitada = Modalidad.EN_LINEA in t.modalidades,
                )
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                OpcionTarjeta(
                    "Individual",
                    if (Formato.INDIVIDUAL in t.formatos) "Solo tú y el tutor." else "Este tutor solo da sesiones grupales.",
                    formato == Formato.INDIVIDUAL, { formato = Formato.INDIVIDUAL },
                    habilitada = Formato.INDIVIDUAL in t.formatos,
                )
                OpcionTarjeta(
                    "Grupal",
                    if (Formato.GRUPAL in t.formatos) "De 2 a 5 estudiantes." else "Este tutor solo da sesiones individuales.",
                    formato == Formato.GRUPAL, { formato = Formato.GRUPAL },
                    habilitada = Formato.GRUPAL in t.formatos,
                )
                if (formato == Formato.GRUPAL) {
                    CampoTexto(
                        etiqueta = "Correos de compañeros (solo grupal)",
                        valor = correos,
                        onCambio = { correos = it },
                        placeholder = "compañero@alu.uabcs.mx",
                        ayuda = "Separa los correos con coma. Máximo 4, todos institucionales.",
                        esError = correos.isNotBlank() && !correosValidos,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }
            }
            3 -> {
                Text("Cuéntale al tutor qué necesitas", style = MaterialTheme.typography.headlineSmall)
                Text("Materia", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    t.materias.forEach { m -> FilterChip(materia == m, { materia = m }, { Text(m) }) }
                }
                CampoTexto(etiqueta = "Tema o duda", valor = tema, onCambio = { tema = it }, placeholder = "Ej. Límites y continuidad")
                CampoTexto(
                    etiqueta = "Objetivo de la sesión",
                    valor = objetivo,
                    onCambio = { objetivo = it },
                    placeholder = "Qué quieres lograr al terminar la sesión",
                    ayuda = "Mínimo 20 caracteres.",
                    esError = objetivo.isNotEmpty() && objetivo.trim().length < 20,
                    minLineas = 3,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Adjunta ejercicios o avances", style = MaterialTheme.typography.labelMedium)
                    Text("PDF, JPG o PNG · máximo 10 MB · opcional", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (archivo == null) {
                        BotonSecundario(
                            onClick = { selector.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Icon(Icons.Outlined.AttachFile, null, Modifier.padding(end = 8.dp))
                            Text("Elegir archivo")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                            Text(archivo!!, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(start = 8.dp))
                            IconButton(onClick = { archivo = null }) { Icon(Icons.Outlined.Close, contentDescription = "Quitar archivo") }
                        }
                    }
                    errorArchivo?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
            else -> {
                Text("Elige horario y confirma", style = MaterialTheme.typography.headlineSmall)
                if (libres.isEmpty()) {
                    Tarjeta {
                        Icon(Icons.Outlined.EventBusy, null, tint = TemaUabcs.colores.espera)
                        Text("${t.nombre} no tiene horarios libres en las próximas 2 semanas", style = MaterialTheme.typography.titleMedium)
                        Text("Únete a la lista de espera y te avisaremos en cuanto se libere un lugar.", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { enviar(null) }, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            Text("Unirme a lista de espera")
                        }
                    }
                    val otros = Tutorias.parecidos(t)
                    if (otros.isNotEmpty()) {
                        Text("Otros tutores de esta área", style = MaterialTheme.typography.headlineSmall)
                        otros.forEach { o -> TarjetaTutor(o) { onOtroTutor(o.id) } }
                    }
                } else {
                    VistaHorarios(t, horario, onElegir = { horario = it })
                    Tarjeta(titulo = "Resumen de tu solicitud") {
                        DatoFila("Tipo", tipo?.nombre.orEmpty())
                        DatoFila("Modalidad", "${modalidad?.nombre} · ${formato?.nombre}")
                        DatoFila("Materia y tema", "$materia · ${tema.trim()}")
                        DatoFila("Archivo", archivo ?: "Sin archivo")
                        DatoFila("Fecha y hora", horario?.let(::fechaHora) ?: "Elige un horario")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonSecundario(
                onClick = { if (paso == 1) onCancelar() else paso-- },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f).height(48.dp),
            ) { Text(if (paso == 1) "Cancelar" else "Atrás") }
            if (paso < 4 || libres.isNotEmpty()) {
                Button(
                    onClick = { if (paso < 4) paso++ else enviar(horario) },
                    enabled = puedeSeguir,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text(if (paso < 4) "Continuar" else "Enviar solicitud") }
            }
        }
    }
}

@Composable
private fun Progreso(paso: Int) {
    Column(
        Modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(paso.toFloat(), 0f..4f, steps = 3) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row {
            Text("Paso $paso de 4 · ${TITULOS_PASO[paso - 1]}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text("$paso/4", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier.weight(1f).height(4.dp).background(
                        if (i < paso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(2.dp),
                    ),
                )
            }
        }
    }
}

@Composable
fun SolicitudEnviada(id: String, onMisSolicitudes: () -> Unit, onInicio: () -> Unit, modifier: Modifier = Modifier) {
    val s = Tutorias.solicitud(id) ?: return
    val t = Tutorias.tutor(s.tutorId)
    val c = TemaUabcs.colores
    val contexto = LocalContext.current
    val (icono, color, subtitulo, detalle) = when (s.estado) {
        Estado.CONFIRMADA -> Quad(
            Icons.Outlined.CheckCircle, c.exito, "Confirmada automáticamente",
            "El horario estaba libre, así que tu sesión quedó confirmada. Te enviaremos recordatorios 24 h y 1 h antes.",
        )
        Estado.LISTA_ESPERA -> Quad(
            Icons.Outlined.HourglassTop, c.espera, "Estás en la lista de espera",
            "Quedaste en la ${s.nota}. Te avisaremos en cuanto ${t.nombre} libere un horario.",
        )
        else -> Quad(
            Icons.Outlined.HourglassTop, c.advertencia, "Pendiente de aceptación del tutor",
            "${t.nombre} tiene hasta 24 horas para aceptar. Te avisaremos por notificación y correo institucional.",
        )
    }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(56.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Solicitud enviada", style = MaterialTheme.typography.headlineLarge)
            Text(subtitulo, style = MaterialTheme.typography.titleMedium, color = color)
            Text(detalle, style = MaterialTheme.typography.bodyLarge)
        }
        TarjetaCita(s, onDetalle = onMisSolicitudes, onMensajes = {}, conAcciones = false)
        Button(onClick = onMisSolicitudes, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Ver mis solicitudes")
        }
        val inicio = s.inicio
        if (s.estado == Estado.CONFIRMADA && inicio != null) {
            BotonSecundario(
                onClick = {
                    val ms = inicio.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, s.titulo)
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, lugar(s, t))
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, ms)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, ms + 3_600_000)
                    try {
                        contexto.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(contexto, "No hay una app de calendario instalada.", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Agregar a mi calendario") }
        }
        TextButton(onClick = onInicio, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Volver al inicio") }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
