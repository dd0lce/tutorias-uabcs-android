package mx.uabcs.tutorias.ui.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.uabcs.tutorias.data.Tutorias
import mx.uabcs.tutorias.ui.BotonSecundario
import mx.uabcs.tutorias.ui.CampoTexto
import mx.uabcs.tutorias.ui.estudiante.Tarjeta

@Composable
fun SesionesTutor(tutorId: String, avisar: (String) -> Unit, modifier: Modifier = Modifier) {
    val porRegistrar = Tutorias.porRegistrar(tutorId)
    val realizadas = Tutorias.realizadas(tutorId)
    var registrando by rememberSaveable { mutableStateOf<String?>(null) }
    var asistio by rememberSaveable { mutableStateOf(true) }
    var notas by rememberSaveable { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Por registrar (${porRegistrar.size})", style = MaterialTheme.typography.headlineSmall)
        if (porRegistrar.isEmpty()) {
            Text(
                "No tienes sesiones pasadas por registrar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        porRegistrar.forEach { s ->
            Tarjeta {
                DatosSolicitud(s)
                if (registrando == s.id) {
                    Row(
                        Modifier.fillMaxWidth().toggleable(asistio, role = Role.Checkbox, onValueChange = { asistio = it }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = asistio, onCheckedChange = null)
                        Text("El estudiante asistió", Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                    CampoTexto(
                        etiqueta = "Notas de la sesión (opcional)",
                        valor = notas,
                        onCambio = { notas = it },
                        placeholder = "Ej. Vimos regla de la cadena; dejar ejercicios 1 a 5.",
                        minLineas = 3,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonSecundario(onClick = { registrando = null }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                Tutorias.registrarSesion(s.id, asistio, notas)
                                registrando = null
                                avisar("Sesión registrada como completada.")
                            },
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Guardar") }
                    }
                } else {
                    Button(
                        onClick = { registrando = s.id; asistio = true; notas = "" },
                        shape = MaterialTheme.shapes.small,
                    ) { Text("Registrar sesión") }
                }
            }
        }
        if (realizadas.isNotEmpty()) {
            Text("Realizadas", style = MaterialTheme.typography.headlineSmall)
            realizadas.forEach { s -> Tarjeta { DatosSolicitud(s) } }
        }
    }
}
