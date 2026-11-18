package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "avisos_institucionales")
class AvisoInstitucional(
    @Id val id: String = "",
    val titulo: String = "",
    val detalle: String = "",
    val hasta: LocalDate = LocalDate.MIN,
    @Column(name = "publicado_en") val publicadoEn: Instant = Instant.now(),
)
