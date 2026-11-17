package mx.uabcs.tutorias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import mx.uabcs.tutorias.data.Api
import mx.uabcs.tutorias.ui.InicioSesionScreen
import mx.uabcs.tutorias.ui.PantallaPrincipal
import mx.uabcs.tutorias.ui.RecuperarContrasena
import mx.uabcs.tutorias.ui.Registro
import mx.uabcs.tutorias.ui.theme.TutoriasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Api.iniciar(applicationContext)
        enableEdgeToEdge()
        setContent { TutoriasTheme { TutoriasApp() } }
    }
}

private enum class SinSesion { INICIO, REGISTRO, RECUPERAR }

@Composable
fun TutoriasApp() {
    val usuario = Api.usuario
    var pantalla by rememberSaveable { mutableStateOf(SinSesion.INICIO) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    if (usuario != null) {
        PantallaPrincipal(usuario, onCerrarSesion = { Api.cerrarSesion(); pantalla = SinSesion.INICIO })
        return
    }
    BackHandler(enabled = pantalla != SinSesion.INICIO) { pantalla = SinSesion.INICIO }
    when (pantalla) {
        SinSesion.INICIO -> InicioSesionScreen(
            aviso = aviso,
            onRegistro = { aviso = null; pantalla = SinSesion.REGISTRO },
            onRecuperar = { aviso = null; pantalla = SinSesion.RECUPERAR },
        )
        SinSesion.REGISTRO -> Registro(onVolver = { pantalla = SinSesion.INICIO })
        SinSesion.RECUPERAR -> RecuperarContrasena(
            onListo = { aviso = "Contraseña actualizada. Inicia sesión con la nueva."; pantalla = SinSesion.INICIO },
            onVolver = { pantalla = SinSesion.INICIO },
        )
    }
}
