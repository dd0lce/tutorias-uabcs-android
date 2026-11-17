package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "mensajes")
class Mensaje(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    val solicitud: Solicitud,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    val autor: Usuario,

    val texto: String,

    @Column(name = "enviado_en")
    val enviadoEn: Instant = Instant.now(),
)
