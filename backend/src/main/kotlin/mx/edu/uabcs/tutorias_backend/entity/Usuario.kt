package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.*
import mx.edu.uabcs.tutorias_backend.model.Rol

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column (nullable = false, length = 150)
    val nombre: String = "",

    @Column(nullable =false, unique = true, length = 255)
    val correo: String = "",

    @Column(name = "password hash", nullable = false, length = 255)
    val passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var rol: Rol = Rol.ESTUDIANTE,

    @Column(nullable = false)
    var activo: Boolean = true

)