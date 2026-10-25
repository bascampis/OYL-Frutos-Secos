package cl.oyl.frutossecos.domain.checkout

/** Datos simples para revisar una compra sin depender de Android ni de la UI. */
data class CheckoutItem(val id: String, val name: String, val quantity: Int, val unitPrice: Int)
data class AvailableItem(val id: String, val name: String, val price: Int, val stock: Int, val active: Boolean)
data class CheckoutReview(val problems: List<String>, val pricesChanged: Boolean) {
    val canConfirm get() = problems.isEmpty() && !pricesChanged
}

object CheckoutRules {
    fun review(items: List<CheckoutItem>, available: List<AvailableItem>): CheckoutReview {
        val problems = mutableListOf<String>()
        if (items.isEmpty()) problems += "Tu carrito está vacío."
        val lookup = available.associateBy { it.id }
        var changed = false
        items.forEach { line ->
            val product = lookup[line.id]
            when {
                line.quantity <= 0 -> problems += "${line.name}: cantidad inválida."
                product == null || !product.active -> problems += "${line.name}: ya no está disponible."
                product.price <= 0 -> problems += "${line.name}: precio no disponible."
                product.stock < line.quantity -> problems += "${line.name}: quedan ${product.stock} unidades; tienes ${line.quantity}."
            }
            if (product != null && product.active && product.price != line.unitPrice) changed = true
        }
        return CheckoutReview(problems, changed)
    }
}
