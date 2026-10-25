package cl.oyl.frutossecos.data.repository

import android.content.Context
import cl.oyl.frutossecos.data.remote.RemoteOrder
import com.google.gson.Gson

/** Conserva la respuesta real del servidor para restaurar la confirmación al reiniciar. */
class OrderReceiptStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("oyl_order_receipts", Context.MODE_PRIVATE)
    private val gson = Gson()
    fun save(owner: String, order: RemoteOrder) {
        require(order.clientAlias == owner) { "El pedido no pertenece a esta sesión." }
        check(preferences.edit().putString("$owner:${order.id}", gson.toJson(order)).commit()) {
            "No se pudo guardar el comprobante local."
        }
    }
    fun read(owner: String, orderId: String): RemoteOrder? = try {
        preferences.getString("$owner:$orderId", null)?.let {
            gson.fromJson(it, RemoteOrder::class.java).takeIf { order -> order.id == orderId && order.clientAlias == owner }
        }
    } catch (_: Exception) { null }
}
