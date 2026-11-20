package cl.oyl.frutossecos.data.remote

/**
 * Dirección de los microservicios Spring Boot.
 * - Emulador de Android Studio: "10.0.2.2" (apunta al PC donde corre Spring Boot).
 * - Celular físico: la IP del PC en el mismo WiFi, por ejemplo "192.168.1.15" (ver con `ipconfig`).
 */
object ApiConfig {
    const val HOST = "192.168.1.8"
    const val CATALOG_URL = "http://$HOST:8081/"
    const val ORDERS_URL = "http://$HOST:8082/"
    const val USERS_URL = "http://$HOST:8083/"
    const val EXCHANGE_URL = "https://open.er-api.com/"
}
