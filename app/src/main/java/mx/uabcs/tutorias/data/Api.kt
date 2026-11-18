package mx.uabcs.tutorias.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import mx.uabcs.tutorias.BuildConfig
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ErrorApi(mensaje: String) : Exception(mensaje)

data class Mensaje(val id: Long, val autor: String, val propio: Boolean, val texto: String, val enviadoEn: Instant)

object Api {
    // Se configura con `api.url` en local.properties (ver app/build.gradle.kts).
    private const val BASE_URL = BuildConfig.BASE_URL

    private var prefs: SharedPreferences? = null
    private var token: String? = null

    /** Usuario con sesión iniciada. La app regresa al inicio de sesión cuando vuelve a `null`. */
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    /** Recupera la sesión guardada. Se llama una vez al abrir la app. */
    fun iniciar(contexto: Context) {
        if (prefs != null) return
        val p = contexto.getSharedPreferences("sesion", Context.MODE_PRIVATE).also { prefs = it }
        // ponytail: token en SharedPreferences sin cifrar; vence en 8 h. Usar Keystore si se guarda algo más sensible.
        token = p.getString("token", null)
        usuario = p.getString("usuario", null)?.let { runCatching { usuarioDe(JSONObject(it)) }.getOrNull() }
        if (token == null || usuario == null) cerrarSesion()
    }

    suspend fun iniciarSesion(correo: String, contrasena: String) = entrar(
        llamar("POST", "/api/auth/login", JSONObject().put("correo", correo.trim().lowercase()).put("password", contrasena)),
    )

    suspend fun pedirCodigoRegistro(correo: String) {
        llamar("POST", "/api/auth/registro/codigo", JSONObject().put("correo", correo.trim().lowercase()))
    }

    suspend fun registrarse(correo: String, codigo: String, nombre: String, detalle: String, contrasena: String) = entrar(
        llamar(
            "POST", "/api/auth/registro",
            JSONObject().put("correo", correo.trim().lowercase()).put("codigo", codigo.trim()).put("nombre", nombre.trim())
                .put("detalle", detalle.trim()).put("password", contrasena),
        ),
    )

    suspend fun pedirCodigoRecuperacion(correo: String) {
        llamar("POST", "/api/auth/recuperar/codigo", JSONObject().put("correo", correo.trim().lowercase()))
    }

    suspend fun recuperar(correo: String, codigo: String, contrasena: String) {
        llamar(
            "POST", "/api/auth/recuperar",
            JSONObject().put("correo", correo.trim().lowercase()).put("codigo", codigo.trim()).put("password", contrasena),
        )
    }

    /**
     * Manda al servidor los [cambios] en orden y recarga los datos cada 30 s, o cada 5 s mientras no haya conexión.
     * Si el servidor rechaza un cambio, avisa y recarga para quitarlo de la pantalla.
     */
    suspend fun sincronizar(cambios: ReceiveChannel<Cambio>, avisar: (String) -> Unit) {
        var recargar = true
        var sinConexion = false
        while (true) {
            if (recargar && cambios.isEmpty) {
                try {
                    // Si entró un cambio mientras llegaba la respuesta, no se pisa: se recarga después de mandarlo.
                    recargar = !cargarDatos { cambios.isEmpty }
                    sinConexion = false
                } catch (e: ErrorApi) {
                    if (!sinConexion) avisar(e.message.orEmpty()) // un aviso por racha, no cada 5 s
                    sinConexion = true
                }
            }
            val c = withTimeoutOrNull(if (recargar) 5_000 else 30_000) { cambios.receive() }
            if (c == null) {
                recargar = true
                continue
            }
            // ponytail: sin cola offline; si no hay conexión el cambio se pierde y se avisa. Guardarlos si hace falta.
            try {
                enviar(c)
            } catch (e: ErrorApi) {
                avisar("No se guardó el cambio: ${e.message}")
            }
            recargar = true
        }
    }

    /** Trae tutores, solicitudes y avisos leídos y reemplaza los locales si [aplicar] lo permite. */
    suspend fun cargarDatos(aplicar: () -> Boolean = { true }): Boolean {
        val d = JSONObject(llamar("GET", "/api/datos"))
        val tutores = d.getJSONArray("tutores").objetos().map(::tutorDe)
        // El servidor las manda de la más vieja a la más nueva; la app muestra primero la más nueva.
        val solicitudes = d.getJSONArray("solicitudes").objetos().map(::solicitudDe).reversed()
        val leidos = d.getJSONArray("leidos").let { a -> List(a.length()) { a.getString(it) } }
        return withContext(Dispatchers.Main) { aplicar().also { if (it) Tutorias.cargar(tutores, solicitudes, leidos) } }
    }

    suspend fun enviar(c: Cambio) {
        llamar("POST", "/api/${c.ruta}", JSONObject(c.datos.mapValues { json(it.value) }))
    }

    suspend fun mensajes(solicitudId: String, despuesDe: Long): List<Mensaje> =
        JSONArray(llamar("GET", "/api/solicitudes/$solicitudId/mensajes?despuesDe=$despuesDe")).objetos().map(::mensajeDe)

    suspend fun enviarMensaje(solicitudId: String, texto: String): Mensaje =
        mensajeDe(JSONObject(llamar("POST", "/api/solicitudes/$solicitudId/mensajes", JSONObject().put("texto", texto))))

    suspend fun descargarArchivo(solicitudId: String): ByteArray = withContext(Dispatchers.IO) {
        val id = URLEncoder.encode(solicitudId, Charsets.UTF_8.name())
        val con = URL("$BASE_URL/api/solicitudes/$id/archivo").openConnection() as HttpURLConnection
        try {
            con.requestMethod = "GET"
            con.connectTimeout = 5_000
            con.readTimeout = 15_000
            token?.let { con.setRequestProperty("Authorization", "******") }
            val codigo = con.responseCode
            if (codigo in 200..299) {
                con.inputStream.use { it.readBytes() }
            } else {
                errorApi(con, codigo, "Inicia sesión para descargar el archivo.")
            }
        } catch (e: IOException) {
            throw ErrorApi("No se pudo descargar el archivo.")
        } finally {
            con.disconnect()
        }
    }

    fun cerrarSesion() {
        token = null
        usuario = null
        prefs?.edit { clear() }
        Tutorias.cargar(emptyList(), emptyList(), emptyList())
    }

    private suspend fun entrar(respuesta: String): Usuario {
        val r = JSONObject(respuesta)
        val u = usuarioDe(r.getJSONObject("usuario"))
        withContext(Dispatchers.Main) {
            token = r.getString("token")
            usuario = u
            prefs?.edit { putString("token", token); putString("usuario", r.getJSONObject("usuario").toString()) }
        }
        return u
    }

    private suspend fun llamar(metodo: String, ruta: String, cuerpo: JSONObject? = null): String = withContext(Dispatchers.IO) {
        val con = URL(BASE_URL + ruta).openConnection() as HttpURLConnection
        try {
            con.requestMethod = metodo
            con.connectTimeout = 5_000
            con.readTimeout = 10_000
            token?.let { con.setRequestProperty("Authorization", "Bearer $it") }
            if (cuerpo != null) {
                con.doOutput = true
                con.setRequestProperty("Content-Type", "application/json")
                con.outputStream.use { it.write(cuerpo.toString().toByteArray()) }
            }
            val codigo = con.responseCode
            if (codigo in 200..299) return@withContext con.inputStream.bufferedReader().use { it.readText() }
            errorApi(con, codigo)
        } catch (e: IOException) {
            throw ErrorApi("No se pudo conectar con el servidor.")
        } catch (e: JSONException) {
            throw ErrorApi("El servidor respondió algo inesperado.")
        } finally {
            con.disconnect()
        }
    }

    private suspend fun errorApi(con: HttpURLConnection, codigo: Int, noAutenticado: String = "Correo o contraseña incorrectos."): Nothing {
        val haySesion = token != null
        val mensaje = con.errorStream?.bufferedReader()?.use { it.readText() }
            ?.let { runCatching { JSONObject(it).optString("mensaje").ifBlank { null } }.getOrNull() }
        if (codigo == 401 && haySesion) {
            withContext(Dispatchers.Main) { cerrarSesion() }
        }
        throw ErrorApi(
            when {
                codigo == 401 && haySesion -> "Tu sesión venció. Inicia sesión de nuevo."
                codigo == 401 -> noAutenticado
                codigo == 403 -> "No tienes permiso para hacer eso."
                codigo == 429 -> "Demasiados intentos. Espera 15 minutos."
                mensaje != null -> mensaje
                else -> "Error del servidor ($codigo)."
            },
        )
    }
}

private fun JSONArray.objetos(): List<JSONObject> = List(length()) { getJSONObject(it) }

private fun JSONArray.textos(): List<String> = List(length()) { getString(it) }

private inline fun <reified E : Enum<E>> JSONArray.enums(): Set<E> = textos().map { enumValueOf<E>(it) }.toSet()

private fun JSONObject.textoONulo(campo: String): String? = if (isNull(campo)) null else getString(campo)

/** Convierte lo que guarda un [Cambio] a algo que entiende `org.json`. */
internal fun json(v: Any?): Any = when (v) {
    null -> JSONObject.NULL
    is Map<*, *> -> JSONObject(v.entries.associate { (k, x) -> k.toString() to json(x) })
    is Iterable<*> -> JSONArray(v.map(::json))
    is Enum<*> -> v.name
    is Boolean, is Number, is String -> v
    else -> v.toString() // LocalDate, LocalDateTime: ISO-8601
}

internal fun usuarioDe(o: JSONObject) = Usuario(
    o.getString("nombre"), o.getString("correo"), Rol.valueOf(o.getString("rol")), o.getString("detalle"), o.textoONulo("tutorId"),
)

internal fun tutorDe(o: JSONObject) = Tutor(
    o.getString("id"), o.getString("nombre"), o.getString("titulo"), o.getString("area"), o.getString("resumen"),
    o.getJSONArray("materias").textos(), o.getDouble("calificacion"), o.getInt("sesiones"),
    o.getJSONArray("modalidades").enums(), o.getJSONArray("formatos").enums(), o.getString("lugar"),
    o.getBoolean("confirmaAutomatico"), o.getJSONArray("experiencia").textos(),
    o.getJSONArray("horarios").textos().map(LocalDateTime::parse),
)

internal fun solicitudDe(o: JSONObject) = Solicitud(
    o.getString("id"), o.getString("tutorId"), TipoSesion.valueOf(o.getString("tipo")),
    Modalidad.valueOf(o.getString("modalidad")), Formato.valueOf(o.getString("formato")),
    o.getJSONArray("companeros").textos(), o.getString("materia"), o.getString("tema"), o.getString("objetivo"),
    o.textoONulo("archivo"), o.textoONulo("inicio")?.let(LocalDateTime::parse), Estado.valueOf(o.getString("estado")),
    o.textoONulo("nota"), o.getBoolean("coordinacionPuedeVer"),
    if (o.isNull("asistio")) null else o.getBoolean("asistio"), o.textoONulo("notasSesion"),
    o.optJSONObject("estudiante")?.let { Estudiante(it.getString("correo"), it.getString("nombre"), it.getString("detalle")) },
    o.textoONulo("creadaEn")?.let(LocalDateTime::parse),
)

internal fun mensajeDe(o: JSONObject) = Mensaje(
    o.getLong("id"), o.getString("autor"), o.getBoolean("propio"), o.getString("texto"), Instant.parse(o.getString("enviadoEn")),
)
