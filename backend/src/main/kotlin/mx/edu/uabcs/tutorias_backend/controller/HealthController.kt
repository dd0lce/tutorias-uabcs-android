package mx.edu.uabcs.tutorias_backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/api/health")
    fun health(): Map<String, String> {
        return mapOf(
                "status" to "OK",
                "application" to "Tutorias UABCS Backend",
                "message" to "Servidor funcionando correctamente"
        )
    }
}
