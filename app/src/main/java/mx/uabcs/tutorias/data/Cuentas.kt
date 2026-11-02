package mx.uabcs.tutorias.data

enum class Rol(val nombre: String) {
    ESTUDIANTE("Estudiante"),
    TUTOR("Tutor"),
    COORDINACION("Coordinación"),
}

// tutorId: id del Tutor en TutoriasRepo; solo para cuentas con rol TUTOR.
data class Usuario(val nombre: String, val correo: String, val rol: Rol, val detalle: String, val tutorId: String? = null)

object Cuentas {
    const val CONTRASENA_DEMO = "Tutorias.2026"

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

    fun buscar(correo: String): Usuario? =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun iniciarSesion(correo: String, contrasena: String): Usuario? =
        buscar(correo)?.takeIf { contrasena == CONTRASENA_DEMO }
}
