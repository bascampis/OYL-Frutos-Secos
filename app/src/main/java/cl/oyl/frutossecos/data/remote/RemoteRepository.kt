package cl.oyl.frutossecos.data.remote

import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.LocalOrderRequest
import cl.oyl.frutossecos.data.model.LocalProduct
import cl.oyl.frutossecos.data.model.OrderStatusDto
import cl.oyl.frutossecos.data.model.ProductInputDto
import cl.oyl.frutossecos.data.model.StockDeltaDto
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

/**
 * Operaciones CRUD contra los 3 microservicios. Cada función devuelve Result:
 * éxito con el dato, o fallo con un mensaje legible para mostrar en la pantalla.
 */
class RemoteRepository(
    private val catalog: CatalogApi = RetrofitProvider.catalogApi,
    private val orders: OrderApi = RetrofitProvider.orderApi,
    private val users: UserApi = RetrofitProvider.userApi
) {
    // ---------- Catálogo ----------
    suspend fun products(): Result<List<LocalProduct>> = apiCall { catalog.products() }
    suspend fun createProduct(p: ProductInputDto): Result<LocalProduct> = apiCall { catalog.create(p) }
    suspend fun updateProduct(p: ProductInputDto): Result<LocalProduct> = apiCall { catalog.update(p.id, p) }
    suspend fun deleteProduct(id: String): Result<Unit> = apiCall {
        val r = catalog.delete(id)
        if (!r.isSuccessful) throw HttpException(r)
    }
    suspend fun adjustStock(id: String, delta: Int): Result<LocalProduct> =
        apiCall { catalog.adjustStock(id, StockDeltaDto(delta)) }

    // ---------- Pedidos ----------
    suspend fun placeOrder(req: LocalOrderRequest): Result<LocalOrder> = apiCall { orders.create(req) }
    suspend fun ordersOf(alias: String): Result<List<LocalOrder>> = apiCall { orders.byClient(alias) }
    suspend fun allOrders(): Result<List<LocalOrder>> = apiCall { orders.all() }
    suspend fun setOrderStatus(id: String, status: String): Result<LocalOrder> =
        apiCall { orders.setStatus(id, OrderStatusDto(status)) }
    suspend fun cancelOrder(id: String): Result<Unit> = apiCall {
        val r = orders.cancel(id)
        if (!r.isSuccessful) throw HttpException(r)
    }

    // ---------- Usuarios ----------
    suspend fun register(body: RegisterBody): Result<RemoteUser> = apiCall { users.register(body) }
    suspend fun login(email: String, password: String): Result<RemoteUser> =
        apiCall { users.login(LoginBody(email, password)) }
    suspend fun users(): Result<List<RemoteUser>> = apiCall { users.users() }
    suspend fun updateUser(id: Long, body: UpdateUserBody): Result<RemoteUser> = apiCall { users.update(id, body) }
    suspend fun deleteUser(id: Long): Result<Unit> = apiCall {
        val r = users.delete(id)
        if (!r.isSuccessful) throw HttpException(r)
    }
}

/** Ejecuta una llamada de red y traduce los errores a mensajes en español. */
internal suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    Result.failure(Exception(httpMessage(e)))
} catch (e: IOException) {
    Result.failure(Exception("No se pudo conectar con el servidor. Revisa que Spring Boot esté encendido."))
} catch (e: Exception) {
    Result.failure(Exception(e.message ?: "Error inesperado"))
}

/** Spring devuelve {"message":"..."}; si no se puede leer, se usa el código HTTP. */
internal fun httpMessage(e: HttpException): String {
    val raw = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
    val parsed = try {
        raw?.let { JsonParser.parseString(it).asJsonObject.get("message")?.asString }
    } catch (_: Exception) { null }
    return parsed?.takeIf { it.isNotBlank() } ?: "Error del servidor (${e.code()})"
}
