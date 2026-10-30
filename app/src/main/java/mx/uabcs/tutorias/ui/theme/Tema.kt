package mx.uabcs.tutorias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ColoresUabcs(
    val fondo: Color,
    val superficie: Color,
    val superficieAlt: Color,
    val divisor: Color,
    val borde: Color,
    val texto: Color,
    val textoSecundario: Color,
    val primario: Color,
    val sobrePrimario: Color,
    val primarioSuave: Color,
    val acento: Color,
    val exito: Color,
    val exitoSuave: Color,
    val advertencia: Color,
    val advertenciaSuave: Color,
    val peligro: Color,
    val peligroSuave: Color,
    val sobrePeligro: Color,
    val info: Color,
    val infoSuave: Color,
    val espera: Color,
    val esperaSuave: Color,
    val foco: Color,
    val skeleton: Color,
    val overlay: Color,
)

val ColoresClaro = ColoresUabcs(
    fondo = Color(0xFFF5F7FA), superficie = Color(0xFFFFFFFF), superficieAlt = Color(0xFFEAEFF5),
    divisor = Color(0xFFD3DAE3), borde = Color(0xFF6B7280), texto = Color(0xFF101828),
    textoSecundario = Color(0xFF475467), primario = Color(0xFF003B71), sobrePrimario = Color(0xFFFFFFFF),
    primarioSuave = Color(0xFFE1ECF8), acento = Color(0xFFC9A227), exito = Color(0xFF15683A),
    exitoSuave = Color(0xFFDCF5E5), advertencia = Color(0xFF8A4B08), advertenciaSuave = Color(0xFFFDF0D2),
    peligro = Color(0xFFB42318), peligroSuave = Color(0xFFFDE4E1), sobrePeligro = Color(0xFFFFFFFF),
    info = Color(0xFF1D4ED8), infoSuave = Color(0xFFDFEAFE), espera = Color(0xFF6D28D9),
    esperaSuave = Color(0xFFEEE6FE), foco = Color(0xFFB45309), skeleton = Color(0xFFE3E8EF),
    overlay = Color(0xFF101828),
)

val ColoresOscuro = ColoresUabcs(
    fondo = Color(0xFF0E141B), superficie = Color(0xFF18212B), superficieAlt = Color(0xFF222C38),
    divisor = Color(0xFF334050), borde = Color(0xFF8B96A5), texto = Color(0xFFF2F4F7),
    textoSecundario = Color(0xFFC0C8D2), primario = Color(0xFF8DBDF0), sobrePrimario = Color(0xFF0E141B),
    primarioSuave = Color(0xFF1C3550), acento = Color(0xFFD9B44A), exito = Color(0xFF86E0A6),
    exitoSuave = Color(0xFF15301F), advertencia = Color(0xFFF5C451), advertenciaSuave = Color(0xFF372A0E),
    peligro = Color(0xFFFDA29B), peligroSuave = Color(0xFF3D1714), sobrePeligro = Color(0xFF0E141B),
    info = Color(0xFF93BBFD), infoSuave = Color(0xFF172A4A), espera = Color(0xFFCDB4FE),
    esperaSuave = Color(0xFF2B1D4A), foco = Color(0xFFFCD34D), skeleton = Color(0xFF2A3541),
    overlay = Color(0xFF000000),
)

private fun ColoresUabcs.aMaterial(oscuro: Boolean): ColorScheme {
    val base = if (oscuro) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primario, onPrimary = sobrePrimario,
        primaryContainer = primarioSuave, onPrimaryContainer = primario,
        secondary = primario, onSecondary = sobrePrimario,
        secondaryContainer = primarioSuave, onSecondaryContainer = primario,
        tertiary = acento, onTertiary = texto,
        background = fondo, onBackground = texto,
        surface = superficie, onSurface = texto,
        surfaceVariant = superficieAlt, onSurfaceVariant = textoSecundario,
        surfaceContainerLowest = superficie, surfaceContainerLow = superficie,
        surfaceContainer = superficie, surfaceContainerHigh = superficieAlt,
        surfaceContainerHighest = superficieAlt,
        outline = borde, outlineVariant = divisor,
        error = peligro, onError = sobrePeligro,
        errorContainer = peligroSuave, onErrorContainer = peligro,
        scrim = overlay,
    )
}

private fun estilo(tamano: Int, alto: Int, peso: FontWeight) =
    TextStyle(fontSize = tamano.sp, lineHeight = alto.sp, fontWeight = peso)

private val Display = estilo(32, 40, FontWeight.Bold)
private val H1 = estilo(24, 32, FontWeight.Bold)
private val H2 = estilo(20, 28, FontWeight.SemiBold)
private val H3 = estilo(17, 24, FontWeight.SemiBold)
private val Cuerpo = estilo(16, 24, FontWeight.Normal)
private val CuerpoFuerte = estilo(16, 24, FontWeight.SemiBold)
private val CuerpoChico = estilo(14, 20, FontWeight.Normal)
private val Etiqueta = estilo(14, 20, FontWeight.Medium)
private val Boton = estilo(16, 20, FontWeight.SemiBold)
private val Leyenda = estilo(12, 16, FontWeight.Normal)
private val LeyendaFuerte = estilo(12, 16, FontWeight.SemiBold)

val Tipografia = Typography(
    displayLarge = Display, displayMedium = Display, displaySmall = Display,
    headlineLarge = H1, headlineMedium = H2, headlineSmall = H3,
    titleLarge = H2, titleMedium = CuerpoFuerte, titleSmall = Etiqueta,
    bodyLarge = Cuerpo, bodyMedium = CuerpoChico, bodySmall = Leyenda,
    labelLarge = Boton, labelMedium = Etiqueta, labelSmall = LeyendaFuerte,
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

private val LocalColoresUabcs = staticCompositionLocalOf { ColoresClaro }

object TemaUabcs {
    val colores: ColoresUabcs
        @Composable get() = LocalColoresUabcs.current
}

@Composable
fun TutoriasTheme(oscuro: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colores = if (oscuro) ColoresOscuro else ColoresClaro
    CompositionLocalProvider(LocalColoresUabcs provides colores) {
        MaterialTheme(
            colorScheme = colores.aMaterial(oscuro),
            typography = Tipografia,
            shapes = Formas,
            content = content,
        )
    }
}
