package cl.oyl.frutossecos.data.local

import android.content.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Avisos locales de demostración separados por alias. No son mensajes FCM push. */
class DemoNotificationStore(context: Context) {
 private val prefs = context.getSharedPreferences("oyl_notifications", Context.MODE_PRIVATE)
 fun append(alias: String, message: String) {
  val key = "notifications_" + alias
  val dated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " · " + message
  val previous = prefs.getString(key,"").orEmpty()
  prefs.edit().putString(key, (dated + "\n" + previous).take(6000)).apply()
 }
 fun list(alias: String): List<String> = prefs.getString("notifications_" + alias,"")
  .orEmpty().lineSequence().filter { it.isNotBlank() }.toList()
}