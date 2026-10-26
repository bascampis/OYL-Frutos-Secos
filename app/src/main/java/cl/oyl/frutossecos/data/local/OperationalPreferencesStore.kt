package cl.oyl.frutossecos.data.local

import android.content.Context

data class OperationalPreferences(
    val minimumStock: Int = 5,
    val notifyNewOrders: Boolean = true,
    val notifyLowStock: Boolean = true
)

// Validaciones separadas de la interfaz.
object OperationalPreferencesValidator {

    fun minimumStockError(value: String): String? {
        val number = value.trim().toIntOrNull()

        return when {
            value.isBlank() ->
                "Ingresa el stock mínimo."

            number == null ->
                "Ingresa un número entero válido."

            number < 0 ->
                "El stock mínimo no puede ser negativo."

            else -> null
        }
    }
}

class OperationalPreferencesStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "oyl_operational_preferences",
            Context.MODE_PRIVATE
        )

    fun read(alias: String): OperationalPreferences {
        return OperationalPreferences(
            minimumStock = preferences.getInt(
                "minimumStock:$alias",
                5
            ),
            notifyNewOrders = preferences.getBoolean(
                "notifyNewOrders:$alias",
                true
            ),
            notifyLowStock = preferences.getBoolean(
                "notifyLowStock:$alias",
                true
            )
        )
    }

    fun save(
        alias: String,
        value: OperationalPreferences
    ) {
        require(alias.isNotBlank()) {
            "No se encontró el usuario."
        }

        require(value.minimumStock >= 0) {
            "El stock mínimo no puede ser negativo."
        }

        val saved = preferences.edit()
            .putInt(
                "minimumStock:$alias",
                value.minimumStock
            )
            .putBoolean(
                "notifyNewOrders:$alias",
                value.notifyNewOrders
            )
            .putBoolean(
                "notifyLowStock:$alias",
                value.notifyLowStock
            )
            .commit()

        check(saved) {
            "No se pudieron guardar las preferencias."
        }
    }
}