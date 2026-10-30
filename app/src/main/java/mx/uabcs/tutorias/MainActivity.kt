package mx.uabcs.tutorias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import mx.uabcs.tutorias.data.Cuentas
import mx.uabcs.tutorias.ui.InicioSesionScreen
import mx.uabcs.tutorias.ui.PantallaPrincipal
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TutoriasTheme { TutoriasApp() } }
    }
}

@Composable
fun TutoriasApp() {
    var correo by rememberSaveable { mutableStateOf<String?>(null) }
    val usuario = correo?.let(Cuentas::buscar)
    if (usuario == null) {
        InicioSesionScreen(onIngresar = { correo = it.correo })
    } else {
        PantallaPrincipal(usuario, onCerrarSesion = { correo = null })
    }
}
