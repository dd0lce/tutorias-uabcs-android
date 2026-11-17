package mx.edu.uabcs.tutorias_backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.model.Formato
import mx.edu.uabcs.tutorias_backend.model.Modalidad

@Entity
@Table(name = "tutores")
class Tutor(
    @Id
    @Column(length = 40)
    val id: String = "",

    @OneToOne
    @JoinColumn(name = "usuario_id")
    var usuario: Usuario? = null,

    var nombre: String = "",
    var titulo: String = "",
    var area: String = "",
    var resumen: String = "",
    var calificacion: Double = 0.0,
    var sesiones: Int = 0,
    var lugar: String = "",

    @Column(name = "confirma_automatico")
    var confirmaAutomatico: Boolean = false,

    @ElementCollection
    @CollectionTable(name = "tutor_materias", joinColumns = [JoinColumn(name = "tutor_id")])
    @OrderColumn(name = "posicion")
    @Column(name = "materia")
    val materias: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "tutor_experiencia", joinColumns = [JoinColumn(name = "tutor_id")])
    @OrderColumn(name = "posicion")
    @Column(name = "texto")
    val experiencia: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "tutor_modalidades", joinColumns = [JoinColumn(name = "tutor_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad")
    val modalidades: MutableSet<Modalidad> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "tutor_formatos", joinColumns = [JoinColumn(name = "tutor_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "formato")
    val formatos: MutableSet<Formato> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "tutor_horarios", joinColumns = [JoinColumn(name = "tutor_id")])
    @Column(name = "inicio")
    val horarios: MutableSet<LocalDateTime> = mutableSetOf(),
)
