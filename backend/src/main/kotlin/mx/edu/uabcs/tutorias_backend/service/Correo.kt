package mx.edu.uabcs.tutorias_backend.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

/**
 * Manda correos con SMTP si `spring.mail.host` está configurado.
 * Sin SMTP, en el perfil `dev` el correo se escribe en el log para poder probar sin servidor de correo.
 */
@Component
class Correo(
    private val smtp: ObjectProvider<JavaMailSender>,
    private val entorno: Environment,
    @Value("\${app.correo.remitente:no-responder@uabcs.mx}") private val remitente: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun enviar(para: String, asunto: String, texto: String) {
        val mail = smtp.ifAvailable
        when {
            mail != null -> mail.send(SimpleMailMessage().apply {
                from = remitente
                setTo(para)
                subject = asunto
                this.text = texto
            })
            entorno.matchesProfiles("dev") -> log.info("[correo dev] Para: {} · {} · {}", para, asunto, texto)
            else -> throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El envío de correos no está configurado.")
        }
    }
}
