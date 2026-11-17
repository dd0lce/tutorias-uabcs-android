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

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var rol: Rol = Rol.ESTUDIANTE,

    @Column(nullable = false)
    var activo: Boolean = true,

    /** Carrera y semestre del estudiante, o departamento del tutor. */
    @Column(nullable = false, length = 200)
    var detalle: String = "",

    @ElementCollection
    @CollectionTable(name = "avisos_leidos", joinColumns = [JoinColumn(name = "usuario_id")])
    @Column(name = "aviso_id", length = 80)
    val leidos: MutableSet<String> = mutableSetOf(),
)
