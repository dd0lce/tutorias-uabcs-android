
package mx.edu.uabcs.tutorias_backend.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DatabaseController(
    private val jdbcTemplate: JdbcTemplate
) {

    @GetMapping("/api/database/health")
    fun databaseHealth(): Map<String, String> {

        val database = jdbcTemplate.queryForObject(
            "SELECT current_database()",
            String::class.java
        )

        val user = jdbcTemplate.queryForObject(
            "SELECT current_user",
            String::class.java
        )

        return mapOf(
            "status" to "OK",
            "database" to (database ?: "unknown"),
            "user" to (user ?: "unknown")
        )
    }
}
