
package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "estudiantes")
class Estudiante(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 150)
    var nombre: String = "",

    @Column(nullable = false, unique = true, length = 255)
    var correo: String = ""
)

