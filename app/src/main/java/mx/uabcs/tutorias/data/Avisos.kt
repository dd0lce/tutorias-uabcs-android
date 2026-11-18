package mx.uabcs.tutorias.data

enum class TipoAviso { CONFIRMADA, RECHAZADA, LISTA_ESPERA, NUEVA, CAMBIO, POR_REGISTRAR, HORARIO_LIBRE, RECORDATORIO, RECORDATORIO_HORA }

data class Aviso(val tipo: TipoAviso, val solicitud: Solicitud) {
    val id get() = "${solicitud.id}:$tipo"
}

/** Aviso que publica Coordinación en el Inicio de estudiantes y tutores. Se muestra hasta [hasta], incluido. */
data class AvisoInstitucional(val id: String, val titulo: String, val detalle: String, val hasta: java.time.LocalDate)
