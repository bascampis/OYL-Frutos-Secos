package cl.oyl.frutossecos.navigation

object Routes {
    const val WELCOME = "welcome"
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val MAP = "map"
    const val CATALOG = "catalog"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    fun productDetail(productId: String) = "product_detail/$productId"
    const val QR = "qr"
    const val CART = "cart"
    const val ORDER_CONFIRMATION = "order_confirmation/{orderId}"
    fun orderConfirmation(orderId: String) = "order_confirmation/$orderId"
    const val PRODUCT_MANAGEMENT = "product_management"
    const val INVENTORY_MANAGEMENT = "inventory_management"
    const val ORDERS = "orders"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val ABOUT = "about"
}
