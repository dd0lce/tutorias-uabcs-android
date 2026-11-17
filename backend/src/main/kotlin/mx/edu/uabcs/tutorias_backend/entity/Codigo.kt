package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "codigos")
class Codigo(
    @Id
    val correo: String,

    val proposito: String,

    @Column(name = "codigo_hash")
    val codigoHash: String,

    val expira: Instant,

    var intentos: Int = 0,
)
