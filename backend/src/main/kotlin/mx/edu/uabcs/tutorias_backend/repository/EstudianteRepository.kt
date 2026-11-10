
package mx.edu.uabcs.tutorias_backend.repository

import mx.edu.uabcs.tutorias_backend.entity.Estudiante
import org.springframework.data.jpa.repository.JpaRepository

interface EstudianteRepository : JpaRepository<Estudiante, Long>
