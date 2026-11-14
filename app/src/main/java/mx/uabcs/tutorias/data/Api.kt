package mx.uabcs.tutorias.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ErrorApi(mensaje: String) : Exception(mensaje)

object Api {
    // Solo sirve en el emulador (10.0.2.2 = tu Mac). En un teléfono físico usa la IP de tu Mac.
    const val BASE_URL = "http://10.0.2.2:8081"

    // Solo en memoria: se pierde si Android cierra la app.
    var token: String? = null
        private set

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario = withContext(Dispatchers.IO) {
        val c = correo.trim().lowercase()
        val con = URL("$BASE_URL/api/auth/login").openConnection() as HttpURLConnection
        try {
            con.requestMethod = "POST"
            con.connectTimeout = 5_000
            con.readTimeout = 5_000
            con.doOutput = true
            con.setRequestProperty("Content-Type", "application/json")
            val cuerpo = JSONObject().put("correo", c).put("password", contrasena).toString()
            con.outputStream.use { it.write(cuerpo.toByteArray()) }

            when (con.responseCode) {
                200 -> {
                    token = JSONObject(con.inputStream.bufferedReader().use { it.readText() }).getString("token")
                    // Tutores y solicitudes siguen siendo locales, así que el usuario se toma de Cuentas.
                    Cuentas.buscar(c) ?: throw ErrorApi("Tu cuenta todavía no tiene datos en la app.")
                }
                401 -> throw ErrorApi("Correo o contraseña incorrectos.")
                429 -> throw ErrorApi("Demasiados intentos. Espera 15 minutos.")
                else -> throw ErrorApi("Error del servidor (${con.responseCode}).")
            }
        } catch (e: IOException) {
            throw ErrorApi("No se pudo conectar con el servidor.")
        } finally {
            con.disconnect()
        }
    }

    fun cerrarSesion() {
        token = null
    }
}