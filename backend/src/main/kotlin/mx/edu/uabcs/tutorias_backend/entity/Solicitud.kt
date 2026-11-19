package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.model.Estado
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad
import mx.edu.uabcs.tutorias_backend.model.TipoSesion

@Entity
@Table(name = "solicitudes")
class Solicitud(
    @Id
    @Column(length = 40)
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    val tutor: Tutor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    val estudiante: Usuario,

    @Enumerated(EnumType.STRING) val tipo: TipoSesion,
    @Enumerated(EnumType.STRING) val modalidad: Modalidad,
    @Enumerated(EnumType.STRING) val formato: Formato,

    @ElementCollection
    @CollectionTable(name = "solicitud_companeros", joinColumns = [JoinColumn(name = "solicitud_id")])
    @OrderColumn(name = "posicion")
    @Column(name = "correo")
    val companeros: MutableList<String> = mutableListOf(),

    val materia: String,
    val tema: String,
    val objetivo: String,
    val archivo: String?,
    var inicio: LocalDateTime?,

    @Enumerated(EnumType.STRING)
    var estado: Estado,

    var nota: String? = null,

    @Column(name = "coordinacion_puede_ver")
    var coordinacionPuedeVer: Boolean = false,

    var asistio: Boolean? = null,

    @Column(name = "notas_sesion")
    var notasSesion: String? = null,

    @Column(name = "creada_en")
    val creadaEn: Instant = Instant.now(),

    /** Hora de La Paz en que quedó [Estado.PENDIENTE]; ver `ServicioTutorias.vencerPendientes`. */
    @Column(name = "pendiente_desde")
    var pendienteDesde: LocalDateTime? = null,

    /** Horario que propone el tutor; el estudiante lo acepta con `aceptar-propuesta`. */
    var propuesta: LocalDateTime? = null,
) {
    val activa: Boolean get() = estado == Estado.PENDIENTE || estado == Estado.CONFIRMADA
}
