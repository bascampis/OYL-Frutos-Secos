package cl.oyl.frutossecos.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.oyl.frutossecos.MainActivity
import cl.oyl.frutossecos.data.remote.OylNetwork
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONObject
import java.time.Instant

data class LocalNotice(
 val id: String,
 val title: String,
 val message: String,
 val category: String,
 val target: String,
 val createdAt: String,
 val read: Boolean = false
)

class DemoNotificationStore(context: Context) {

 private val app = context.applicationContext

 private val prefs = app.getSharedPreferences(
  "oyl_local_notices",
  Context.MODE_PRIVATE
 )

 private val session = app.getSharedPreferences(
  "oyl_session",
  Context.MODE_PRIVATE
 )

 private val gson = Gson()

 fun list(alias: String): List<LocalNotice> {
  return runCatching {
   gson.fromJson(
    prefs.getString("list:$alias", "[]"),
    Array<LocalNotice>::class.java
   )?.toList().orEmpty()
  }.getOrDefault(emptyList())
 }

 fun observe(alias: String): Flow<List<LocalNotice>> = callbackFlow {
  val listener =
   SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (key == "list:$alias") {
     trySend(list(alias))
    }
   }

  prefs.registerOnSharedPreferenceChangeListener(listener)

  trySend(list(alias))

  awaitClose {
   prefs.unregisterOnSharedPreferenceChangeListener(listener)
  }
 }.distinctUntilChanged()

 fun markRead(alias: String, id: String) {
  val updated = list(alias).map { notice ->
   if (notice.id == id) {
    notice.copy(read = true)
   } else {
    notice
   }
  }

  save(alias, updated)
 }

 private fun save(
  alias: String,
  notices: List<LocalNotice>
 ) {
  prefs.edit()
   .putString(
    "list:$alias",
    gson.toJson(notices.take(100))
   )
   .apply()
 }

 private fun add(
  alias: String,
  id: String,
  title: String,
  message: String,
  category: String,
  target: String
 ) {
  val previous = list(alias)

  if (previous.any { it.id == id }) {
   return
  }

  val notice = LocalNotice(
   id = id,
   title = title,
   message = message,
   category = category,
   target = target,
   createdAt = Instant.now().toString()
  )

  // El historial se guarda aunque los avisos estén desactivados.
  save(alias, listOf(notice) + previous)

  if (session.getString("alias", null) == alias) {
   showSystemNotification(alias, notice)
  }
 }

 suspend fun refresh(): Boolean {
  val alias = session.getString("alias", null) ?: return true

  val admin =
   session.getString("role", "") == "ADMINISTRADOR"

  return try {
   val orders = if (admin) {
    OylNetwork.api.allOrders()
   } else {
    OylNetwork.api.orders(alias)
   }

   if (session.getString("alias", null) != alias) {
    return true
   }

   val previousText = prefs.getString(
    "orders:$alias",
    null
   )

   val previous = previousText?.let {
    JSONObject(it)
   }

   val current = JSONObject()

   orders.forEach { order ->
    current.put(order.id, order.status)

    // La primera consulta establece el punto de partida.
    // No genera avisos por pedidos antiguos.
    if (previous != null) {
     val exists = previous.has(order.id)
     val oldStatus = previous.optString(order.id)

     if (!exists) {
      add(
       alias = alias,
       id = "order:${order.id}:new",
       title = if (admin) {
        "Nuevo pedido recibido"
       } else {
        "Solicitud recibida"
       },
       message = "Pedido #${order.id.take(8)} · ${order.status}",
       category = "Pedidos",
       target = "orders"
      )
     } else if (
      !admin &&
      oldStatus != order.status
     ) {
      add(
       alias = alias,
       id = "order:${order.id}:${order.status}",
       title = "Estado del pedido actualizado",
       message = "Pedido #${order.id.take(8)}: ${order.status}",
       category = "Pedidos",
       target = "orders"
      )
     }
    }
   }

   prefs.edit()
    .putString(
     "orders:$alias",
     current.toString()
    )
    .apply()

   if (admin) {
    checkStock(alias)
   }

   true
  } catch (e: CancellationException) {
   throw e
  } catch (_: Exception) {
   false
  }
 }

 private suspend fun checkStock(alias: String) {
  val products = OylNetwork.api.products()

  if (session.getString("alias", null) != alias) {
   return
  }

  val previousText = prefs.getString(
   "stock:$alias",
   null
  )

  val previous = previousText?.let {
   JSONObject(it)
  }

  val current = JSONObject()

  products.filter { it.active }.forEach { product ->
   current.put(product.id, product.stock)

   val wasAboveMinimum =
    previous?.optInt(
     product.id,
     Int.MAX_VALUE
    )?.let {
     it > 5
    } ?: true

   if (
    product.stock <= 5 &&
    wasAboveMinimum
   ) {
    add(
     alias = alias,
     id = "stock:${product.id}:${System.currentTimeMillis()}",
     title = "Producto con stock bajo",
     message = "${product.name}: quedan ${product.stock} unidades.",
     category = "Inventario",
     target = "inventory"
    )
   }
  }

  prefs.edit()
   .putString(
    "stock:$alias",
    current.toString()
   )
   .apply()
 }

 private fun showSystemNotification(
  alias: String,
  notice: LocalNotice
 ) {
  // Solo muestra avisos del usuario conectado.
  if (session.getString("alias", null) != alias) {
   return
  }

  // Respeta el interruptor guardado en Perfil.
  if (!prefs.getBoolean("enabled:$alias", true)) {
   return
  }

  if (
   Build.VERSION.SDK_INT >= 33 &&
   ContextCompat.checkSelfPermission(
    app,
    Manifest.permission.POST_NOTIFICATIONS
   ) != PackageManager.PERMISSION_GRANTED
  ) {
   return
  }

  val manager = app.getSystemService(
   NotificationManager::class.java
  )

  manager.createNotificationChannel(
   NotificationChannel(
    "oyl_local_events",
    "Avisos de OYL",
    NotificationManager.IMPORTANCE_DEFAULT
   )
  )

  val intent = Intent(
   app,
   MainActivity::class.java
  ).apply {
   flags =
    Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP

   // Identifica cada aviso para conservar su destino.
   data = Uri.Builder()
    .scheme("oyl")
    .authority("notification")
    .appendPath(alias)
    .appendPath(notice.id)
    .build()

   putExtra(
    "notification_target",
    notice.target
   )

   putExtra(
    "notification_alias",
    alias
   )

   putExtra(
    "notification_id",
    notice.id
   )
  }

  val notificationId = "$alias:${notice.id}".hashCode()

  val pendingIntent = PendingIntent.getActivity(
   app,
   notificationId,
   intent,
   PendingIntent.FLAG_UPDATE_CURRENT or
           PendingIntent.FLAG_IMMUTABLE
  )

  val notification = NotificationCompat.Builder(
   app,
   "oyl_local_events"
  )
   .setSmallIcon(android.R.drawable.ic_dialog_info)
   .setContentTitle(notice.title)
   .setContentText(notice.message)
   .setStyle(
    NotificationCompat.BigTextStyle()
     .bigText(notice.message)
   )
   .setContentIntent(pendingIntent)
   .setAutoCancel(true)
   .build()

  runCatching {
   NotificationManagerCompat.from(app)
    .notify(
     notificationId,
     notification
    )
  }
 }
}