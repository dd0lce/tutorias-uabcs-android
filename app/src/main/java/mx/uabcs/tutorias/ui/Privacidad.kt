package mx.uabcs.tutorias.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.uabcs.tutorias.ui.theme.TemaUabcs

// Borrador: lo debe revisar y completar la Unidad de Transparencia de la UABCS (domicilio y contacto).
private val SECCIONES = listOf(
    "Responsable" to
        "La Universidad Autónoma de Baja California Sur (UABCS), a través de la Coordinación de Tutorías, es responsable " +
        "de los datos personales que se tratan en esta aplicación.",
    "Datos que tratamos" to
        "• Nombre y correo institucional.\n" +
        "• Carrera y semestre (estudiantes) o departamento (tutores).\n" +
        "• Contraseña, guardada cifrada; nadie puede leerla.\n" +
        "• Solicitudes de tutoría: materia, tema, objetivo, horario, modalidad y estado.\n" +
        "• Mensajes entre estudiante y tutor, y archivos que adjuntes a una solicitud.\n" +
        "• Registro de cada sesión: asistencia y acuerdos.\n" +
        "No tratamos datos personales sensibles.",
    "Para qué los usamos" to
        "• Gestionar tus solicitudes y sesiones de tutoría.\n" +
        "• Comunicarte con tu tutor o estudiante y enviarte avisos y recordatorios dentro de la app.\n" +
        "• Generar estadísticas generales para la Coordinación de Tutorías.",
    "Quién puede verlos" to
        "• El tutor ve los datos de las solicitudes que le haces.\n" +
        "• Coordinación ve datos generales de las sesiones. Solo ve el contenido académico (tema, objetivo, archivo y acuerdos) " +
        "si lo autorizas en la solicitud.\n" +
        "• El correo de un tutor solo aparece en su perfil si él lo activa.\n" +
        "No transferimos tus datos fuera de la UABCS, salvo que una autoridad competente lo requiera conforme a la ley.",
    "Tus derechos" to
        "Puedes acceder a tus datos, rectificarlos, cancelarlos u oponerte a su tratamiento (derechos ARCO) con una solicitud a la " +
        "Unidad de Transparencia de la UABCS. Tu nombre y tu carrera o departamento también los puedes corregir en Perfil › Editar perfil.",
    "Cambios a este aviso" to
        "Si este aviso cambia, publicaremos la nueva versión en esta misma pantalla.",
)

@Composable
fun AvisoPrivacidadDialogo(onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCerrar) { Icon(Iconos.cerrar, contentDescription = "Cerrar") }
                    Text("Aviso de privacidad", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                }
                Pantalla {
                    Surface(shape = MaterialTheme.shapes.medium, color = TemaUabcs.colores.advertenciaSuave) {
                        Text(
                            "Borrador pendiente de revisión por la UABCS.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TemaUabcs.colores.advertencia,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    SECCIONES.forEach { (titulo, texto) ->
                        Tarjeta(titulo = titulo) { Text(texto, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
    }
}
