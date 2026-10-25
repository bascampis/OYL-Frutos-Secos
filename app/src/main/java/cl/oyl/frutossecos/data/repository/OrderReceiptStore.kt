package cl.oyl.frutossecos.data.repository

import android.content.Context
import cl.oyl.frutossecos.data.model.LocalOrder
import com.google.gson.Gson

class OrderReceiptStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "oyl_order_receipts",
            Context.MODE_PRIVATE
        )

    private val gson = Gson()

    fun save(
        owner: String,
        order: LocalOrder
    ) {
        require(order.clientAlias == owner) {
            "El pedido no pertenece a esta sesión."
        }

        val saved = preferences.edit()
            .putString(
                "$owner:${order.id}",
                gson.toJson(order)
            )
            .commit()

        check(saved) {
            "No se pudo guardar el comprobante local."
        }
    }

    fun read(
        owner: String,
        orderId: String
    ): LocalOrder? {
        return try {
            preferences.getString(
                "$owner:$orderId",
                null
            )?.let { json ->
                gson.fromJson(
                    json,
                    LocalOrder::class.java
                ).takeIf {
                    it.id == orderId &&
                            it.clientAlias == owner
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}