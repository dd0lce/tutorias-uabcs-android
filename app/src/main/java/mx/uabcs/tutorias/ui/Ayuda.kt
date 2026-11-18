package mx.uabcs.tutorias.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Rol
import mx.uabcs.tutorias.data.TutoriasRepo.Companion.ANTICIPACION_HORAS
import mx.uabcs.tutorias.data.TutoriasRepo.Companion.DIAS_AGENDA

private fun preguntasDe(rol: Rol): List<Pair<String, String>> = when (rol) {
    Rol.ESTUDIANTE -> listOf(
        "¿Cómo solicito una sesión?" to
            "En Buscar elige un tutor, toca Solicitar sesión y sigue los 4 pasos: tipo, modalidad, detalles y horario.",
        "¿Por qué no veo horarios para hoy?" to
            "Las sesiones se piden con al menos $ANTICIPACION_HORAS h de anticipación y solo se muestran los próximos $DIAS_AGENDA días.",
        "¿Mi sesión ya está confirmada?" to
            "Algunos tutores confirman en automático. Si tu solicitud dice Pendiente, el tutor tiene que aceptarla y te avisaremos.",
        "El tutor no tiene horarios libres" to
            "Únete a la lista de espera y te avisaremos cuando se libere un lugar, o elige otro tutor de la misma área.",
        "¿Cómo cambio o cancelo una cita?" to
            "En Mis solicitudes abre la cita. Puedes elegir otro horario libre, pedirle el cambio al tutor o cancelarla.",
        "¿Cómo armo una sesión grupal?" to
            "Elige Grupal en el paso 2 y escribe los correos institucionales de tus compañeros separados por coma (máximo 4).",
    )
    Rol.TUTOR -> listOf(
        "¿Cómo respondo una solicitud?" to
            "En Solicitudes, pestaña Nuevas, toca Aceptar o Rechazar. Si rechazas, elige el motivo; el estudiante lo verá.",
        "¿Qué pasa si no respondo una solicitud?" to
            "Tienes hasta $ANTICIPACION_HORAS h para responder, o hasta la hora de la sesión si llega antes. Si vence el plazo, se rechaza automáticamente y el horario queda libre.",
        "¿Cómo abro o cierro horarios?" to
            "En Inicio toca Editar en Mi horario. Toca cada bloque de la cuadrícula para abrirlo o cerrarlo y luego Guardar cambios. Con Copiar a la próxima semana repites el mismo horario. Las horas con una sesión agendada no se pueden cerrar.",
        "¿Cuándo registro una sesión?" to
            "Cuando ya pasó. En Sesiones ábrela desde Por registrar y toca Registrar sesión: marca si el estudiante asistió y anota los acuerdos.",
        "¿Por qué mi horario no le sale al estudiante?" to
            "Los estudiantes solo ven horarios con al menos $ANTICIPACION_HORAS h de anticipación.",
    )
    Rol.COORDINACION -> listOf(
        "¿Qué puedo hacer en esta app?" to
            "Consultar el resumen de solicitudes, las sesiones autorizadas y el directorio de tutores.",
        "¿Puedo solicitar sesiones?" to
            "No. Solicitar sesiones es solo para estudiantes; desde el directorio puedes ver el perfil de cada tutor.",
    )
}

@Composable
fun Ayuda(rol: Rol, modifier: Modifier = Modifier) {
    var preguntaAbierta by rememberSaveable(rol) { mutableStateOf<String?>(null) }

    Pantalla(modifier) {
        Text("Preguntas frecuentes", style = MaterialTheme.typography.headlineSmall)
        Tarjeta {
            preguntasDe(rol).forEach { (pregunta, respuesta) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                preguntaAbierta = if (preguntaAbierta == pregunta) null else pregunta
                            },
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            pregunta,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Icon(
                            imageVector = if (preguntaAbierta == pregunta) Iconos.arriba else Iconos.abajo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (preguntaAbierta == pregunta) {
                        Text(respuesta, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Text(
            "¿Algo más? Escribe a la Coordinación de Tutorías de tu departamento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
