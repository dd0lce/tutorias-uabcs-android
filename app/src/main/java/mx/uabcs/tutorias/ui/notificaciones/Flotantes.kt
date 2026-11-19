package mx.uabcs.tutorias.ui.notificaciones

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import mx.uabcs.tutorias.data.Aviso

/**
 * Muestra como notificación del sistema (flotante) cada aviso sin leer que todavía no se mostró.
 * Los ya mostrados se guardan por usuario para no repetirlos al volver a abrir la app.
 */
// ponytail: solo mientras la app está abierta o en segundo plano; con la app cerrada hace falta push (FCM).
object Flotantes {
    private const val CANAL = "avisos"

    fun mostrar(contexto: Context, correo: String, sinLeer: List<Aviso>) {
        val prefs = contexto.getSharedPreferences("flotantes", Context.MODE_PRIVATE)
        val mostrados = prefs.getStringSet(correo, emptySet()).orEmpty()
        val nuevos = sinLeer.filter { it.id !in mostrados }
        if (nuevos.isEmpty()) return

        val gestor = contexto.getSystemService(NotificationManager::class.java)
        if (!gestor.areNotificationsEnabled()) return // se muestran cuando el usuario dé el permiso
        gestor.createNotificationChannel(NotificationChannel(CANAL, "Avisos de tutorías", NotificationManager.IMPORTANCE_HIGH))
        val abrir = PendingIntent.getActivity(
            contexto, 0, contexto.packageManager.getLaunchIntentForPackage(contexto.packageName), PendingIntent.FLAG_IMMUTABLE,
        )
        nuevos.forEach { a ->
            val (titulo, detalle) = textoAviso(a)
            gestor.notify(
                a.id.hashCode(),
                Notification.Builder(contexto, CANAL)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(titulo)
                    .setContentText(detalle)
                    .setStyle(Notification.BigTextStyle().bigText(detalle))
                    .setContentIntent(abrir)
                    .setAutoCancel(true)
                    .build(),
            )
        }
        prefs.edit().putStringSet(correo, mostrados + nuevos.map { it.id }).apply()
    }
}
