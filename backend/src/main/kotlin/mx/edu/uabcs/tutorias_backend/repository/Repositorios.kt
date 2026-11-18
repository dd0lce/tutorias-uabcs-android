package mx.edu.uabcs.tutorias_backend.repository

import java.time.LocalDateTime
import mx.edu.uabcs.tutorias_backend.entity.Codigo
import mx.edu.uabcs.tutorias_backend.entity.Mensaje
import mx.edu.uabcs.tutorias_backend.entity.Solicitud
import mx.edu.uabcs.tutorias_backend.entity.Tutor
import mx.edu.uabcs.tutorias_backend.model.Estado
import org.springframework.data.jpa.repository.JpaRepository

interface TutorRepository : JpaRepository<Tutor, String> {
    fun findByUsuarioId(usuarioId: Long): Tutor?
}

interface SolicitudRepository : JpaRepository<Solicitud, String> {
    fun findByEstudianteId(estudianteId: Long): List<Solicitud>
    fun findByTutorId(tutorId: String): List<Solicitud>
    fun findByInicioNotNullAndEstadoIn(estados: Collection<Estado>): List<Solicitud>
    fun existsByTutorIdAndInicioAndEstadoIn(tutorId: String, inicio: LocalDateTime, estados: Collection<Estado>): Boolean
    fun findByEstado(estado: Estado): List<Solicitud>
    fun countByTutorIdAndEstado(tutorId: String, estado: Estado): Long
}

interface MensajeRepository : JpaRepository<Mensaje, Long> {
    fun findBySolicitudIdAndIdGreaterThanOrderById(solicitudId: String, despuesDe: Long): List<Mensaje>
}

interface CodigoRepository : JpaRepository<Codigo, String>
