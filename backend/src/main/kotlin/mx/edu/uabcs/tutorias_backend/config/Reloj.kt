package mx.edu.uabcs.tutorias_backend.config

import java.time.LocalDateTime
import java.time.ZoneId
import org.springframework.stereotype.Component

/** Hora local de La Paz. Las pruebas la cambian para fijar "ahora". */
@Component
class Reloj {
    var ahora: () -> LocalDateTime = { LocalDateTime.now(ZONA) }

    companion object {
        val ZONA: ZoneId = ZoneId.of("America/Mazatlan")
    }
}
