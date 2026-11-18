package mx.uabcs.tutorias.data

enum class Rol(val nombre: String) {
    ESTUDIANTE("Estudiante"),
    TUTOR("Tutor"),
    COORDINACION("Coordinación"),
}

data class Usuario(
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val detalle: String,
    val tutorId: String? = null,
    /** Solo tutores: los estudiantes ven su correo en su perfil. */
    val mostrarCorreo: Boolean = false,
) {
    fun comoEstudiante() = Estudiante(correo, nombre, detalle)
}

object Cuentas {
    // Solo para pruebas y previews; las cuentas reales vienen del backend.
    private val usuarios = listOf(
        Usuario("Derrick Ramírez", "derrick.ramirez@alu.uabcs.mx", Rol.ESTUDIANTE, "Ing. en Desarrollo de Software · 7.º semestre"),
        Usuario("Mariana Cota", "mcota@uabcs.mx", Rol.TUTOR, "Departamento Académico de Matemáticas", tutorId = "mariana"),
        Usuario("Mtra. Laura Peña", "lpena@uabcs.mx", Rol.COORDINACION, "Coordinación de Tutorías"),
    )

    fun esInstitucional(correo: String): Boolean {
        val c = correo.trim().lowercase()
        val arroba = c.indexOf('@')
        return arroba > 0 && c.substring(arroba) in setOf("@uabcs.mx", "@alu.uabcs.mx")
    }

    fun companerosValidos(correos: List<String>, propio: String): Boolean {
        val c = correos.map { it.trim().lowercase() }
        return c.size in 1..4 && c.all(::esInstitucional) && c.distinct().size == c.size &&
            propio.trim().lowercase() !in c
    }

    fun buscar(correo: String): Usuario? =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }
}
