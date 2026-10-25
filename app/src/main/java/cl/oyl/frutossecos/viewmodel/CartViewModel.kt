package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.local.*
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.data.repository.OrderReceiptStore
import cl.oyl.frutossecos.domain.checkout.*
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class CartRow(val item: CartItemEntity, val product: ProductEntity?) {
    val subtotal: Long get() = (product?.price ?: 0).toLong() * item.quantity
    val name get() = product?.name ?: "Producto ${item.productId}"
}
data class CartContact(val name: String, val email: String, val phone: String)

data class CartState(
    val rows: List<CartRow> = emptyList(), val loading: Boolean = true,
    val busy: Boolean = false, val message: String? = null, val reviewOpen: Boolean = false,
    val contact: CartContact? = null, val completedOrder: RemoteOrder? = null
) {
    val subtotal: Long get() = rows.sumOf { it.subtotal }
}

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as OylApplication
    private val repository = app.catalogRepository
    private val receipts = OrderReceiptStore(application)
    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()
    private var owner: String? = null

    fun start(alias: String) {
        if (owner == alias) return
        check(owner == null) { "La sesión del carrito cambió." }
        owner = alias
        viewModelScope.launch {
            try {
                val contact = app.database.clientDao().byAlias(alias)?.let { CartContact(it.name, it.email, it.phone) }
                _state.update { it.copy(contact = contact) }
                combine(repository.cart(alias), app.database.catalogDao().allProducts()) { items, products ->
                    val lookup = products.associateBy { it.id }
                    items.map { CartRow(it, lookup[it.productId]) }
                }.collect { rows -> _state.update { it.copy(rows = rows, loading = false) } }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _state.update { it.copy(loading = false, message = "No se pudo cargar el carrito local.") } }
        }
    }

    fun change(productId: String, delta: Int) {
        val alias = owner ?: return
        if (_state.value.busy || _state.value.completedOrder != null) return
        _state.update { it.copy(busy = true, message = null, reviewOpen = false) }
        viewModelScope.launch {
            try { repository.changeCart(alias, productId, delta) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _state.update { it.copy(message = "No se pudo actualizar el carrito.") } }
            finally { _state.update { it.copy(busy = false) } }
        }
    }

    fun review() {
        if (_state.value.busy || _state.value.loading || _state.value.completedOrder != null) return
        val snapshot = _state.value.rows
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                val remote = OylNetwork.api.products()
                val result = CheckoutRules.review(
                    snapshot.map { CheckoutItem(it.item.productId, it.name, it.item.quantity, it.product?.price ?: 0) },
                    remote.map { AvailableItem(it.id, it.name, it.price, it.stock, it.active) }
                )
                app.database.catalogDao().saveProducts(remote.map {
                    ProductEntity(it.id, it.name, it.category, it.description.orEmpty(), it.presentation.orEmpty(), it.price, it.stock, it.active)
                })
                _state.update { it.copy(
                    reviewOpen = result.canConfirm,
                    message = when {
                        result.problems.isNotEmpty() -> result.problems.joinToString("\n")
                        result.pricesChanged -> "Los precios cambiaron. Se actualizaron en tu carrito; revísalos antes de continuar."
                        else -> null
                    }
                ) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(message = errorMessage(e), reviewOpen = false) } }
            finally { _state.update { it.copy(busy = false) } }
        }
    }

    fun closeReview() {
        if (!_state.value.busy) _state.update { it.copy(reviewOpen = false) }
    }

    fun confirm() {
        val alias = owner ?: return
        val current = _state.value
        if (current.busy || !current.reviewOpen || current.completedOrder != null) return
        val items = current.rows.map { RemoteOrderItem(it.item.productId, it.item.quantity) }
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                // El backend vuelve a validar precio/stock y descuenta dentro de una transacción.
                val order = OylNetwork.api.order(RemoteOrderRequest(alias, "Retiro", items))
                // Desde este momento no se permite reenviar, aunque falle la limpieza local.
                _state.update { it.copy(completedOrder = order, reviewOpen = false) }
                try {
                    withContext(Dispatchers.IO) { receipts.save(alias, order) }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* La confirmación puede recuperar el pedido por la API. */ }
                try { repository.clearCart(alias) }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* El pedido ya existe; no se vuelve a enviar desde esta pantalla. */ }
                repository.syncFromServer()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(message = errorMessage(e), reviewOpen = false) } }
            finally { _state.update { it.copy(busy = false) } }
        }
    }
}

private fun errorMessage(error: Exception): String {
    if (error is HttpException) {
        val detail = runCatching {
            JsonParser.parseString(error.response()?.errorBody()?.string().orEmpty())
                .asJsonObject.get("message")?.asString
        }.getOrNull()
        return detail?.takeIf { it.isNotBlank() } ?: "El servidor rechazó la solicitud. Actualiza el carrito y revisa disponibilidad."
    }
    return "No se pudo completar la comunicación. Si estabas confirmando, revisa Mis pedidos antes de volver a enviar."
}
