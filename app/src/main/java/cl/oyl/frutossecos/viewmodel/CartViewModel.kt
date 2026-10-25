package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.local.CartItemEntity
import cl.oyl.frutossecos.data.local.ProductEntity
import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.LocalOrderItem
import cl.oyl.frutossecos.data.model.LocalOrderRequest
import cl.oyl.frutossecos.data.repository.LocalStore
import cl.oyl.frutossecos.data.repository.OrderReceiptStore
import cl.oyl.frutossecos.domain.checkout.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CartRow(
    val item: CartItemEntity,
    val product: ProductEntity?
) {
    val subtotal: Long
        get() = (product?.price ?: 0).toLong() * item.quantity

    val name: String
        get() = product?.name ?: "Producto ${item.productId}"
}

data class CartContact(
    val name: String,
    val email: String,
    val phone: String
)

data class CartState(
    val rows: List<CartRow> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null,
    val reviewOpen: Boolean = false,
    val contact: CartContact? = null,
    val completedOrder: LocalOrder? = null
) {
    val subtotal: Long
        get() = rows.sumOf { it.subtotal }
}

class CartViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as OylApplication
    private val repository = app.catalogRepository
    private val receipts = OrderReceiptStore(application)

    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

    private var owner: String? = null

    fun start(alias: String) {
        if (owner == alias) return

        check(owner == null) {
            "La sesión del carrito cambió."
        }

        owner = alias

        viewModelScope.launch {
            try {
                val client = app.database.clientDao().byAlias(alias)

                val contact = client?.let {
                    CartContact(
                        name = it.name,
                        email = it.email,
                        phone = it.phone
                    )
                }

                _state.update {
                    it.copy(contact = contact)
                }

                combine(
                    repository.cart(alias),
                    app.database.catalogDao().allProducts()
                ) { items, products ->
                    val lookup = products.associateBy {
                        it.id
                    }

                    items.map {
                        CartRow(
                            item = it,
                            product = lookup[it.productId]
                        )
                    }
                }.collect { rows ->
                    _state.update {
                        it.copy(
                            rows = rows,
                            loading = false
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        message = "No se pudo cargar el carrito local."
                    )
                }
            }
        }
    }

    fun change(
        productId: String,
        delta: Int
    ) {
        val alias = owner ?: return

        if (
            _state.value.busy ||
            _state.value.completedOrder != null
        ) {
            return
        }

        _state.update {
            it.copy(
                busy = true,
                message = null,
                reviewOpen = false
            )
        }

        viewModelScope.launch {
            try {
                repository.changeCart(
                    owner = alias,
                    productId = productId,
                    delta = delta
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        message = "No se pudo actualizar el carrito."
                    )
                }
            } finally {
                _state.update {
                    it.copy(busy = false)
                }
            }
        }
    }

    fun review() {
        val current = _state.value

        if (
            current.busy ||
            current.loading ||
            current.completedOrder != null
        ) {
            return
        }

        val snapshot = current.rows

        _state.update {
            it.copy(
                busy = true,
                message = null
            )
        }

        viewModelScope.launch {
            try {
                val products = LocalStore.repository.products()

                val result = CheckoutRules.review(
                    snapshot.map {
                        CheckoutItem(
                            it.item.productId,
                            it.name,
                            it.item.quantity,
                            it.product?.price ?: 0
                        )
                    },
                    products.map {
                        AvailableItem(
                            it.id,
                            it.name,
                            it.price,
                            it.stock,
                            it.active
                        )
                    }
                )

                _state.update {
                    it.copy(
                        reviewOpen = result.canConfirm,
                        message = when {
                            result.problems.isNotEmpty() -> {
                                result.problems.joinToString("\n")
                            }

                            result.pricesChanged -> {
                                "Los precios cambiaron. Revisa el carrito antes de continuar."
                            }

                            else -> null
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        message = cartErrorMessage(e),
                        reviewOpen = false
                    )
                }
            } finally {
                _state.update {
                    it.copy(busy = false)
                }
            }
        }
    }

    fun closeReview() {
        if (!_state.value.busy) {
            _state.update {
                it.copy(reviewOpen = false)
            }
        }
    }

    fun confirm() {
        val alias = owner ?: return
        val current = _state.value

        if (
            current.busy ||
            !current.reviewOpen ||
            current.completedOrder != null
        ) {
            return
        }

        val items = current.rows.map {
            LocalOrderItem(
                productId = it.item.productId,
                quantity = it.item.quantity
            )
        }

        _state.update {
            it.copy(
                busy = true,
                message = null
            )
        }

        viewModelScope.launch {
            try {
                // Una sola transacción de Room:
                // valida stock, guarda el pedido,
                // descuenta existencias y vacía este carrito.
                val order = LocalStore.repository.order(
                    LocalOrderRequest(
                        clientAlias = alias,
                        fulfillment = "Retiro",
                        items = items
                    )
                )

                _state.update {
                    it.copy(
                        completedOrder = order,
                        reviewOpen = false
                    )
                }

                try {
                    withContext(Dispatchers.IO) {
                        receipts.save(alias, order)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // El pedido ya está guardado en Room.
                    // El comprobante puede recuperarse desde allí.
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        message = cartErrorMessage(e),
                        reviewOpen = false
                    )
                }
            } finally {
                _state.update {
                    it.copy(busy = false)
                }
            }
        }
    }
}

private fun cartErrorMessage(error: Exception): String {
    return error.message?.takeIf {
        it.isNotBlank()
    } ?: "No se pudo guardar el pedido local. Revisa Mis pedidos antes de reintentar."
}