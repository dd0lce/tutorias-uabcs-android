package mx.edu.uabcs.tutorias_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TutoriasBackendApplication

fun main(args: Array<String>) {
    runApplication<TutoriasBackendApplication>(*args)
}
