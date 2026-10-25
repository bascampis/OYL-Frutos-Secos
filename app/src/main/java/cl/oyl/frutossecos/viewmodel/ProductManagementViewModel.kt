package cl.oyl.frutossecos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.model.LocalProduct
import cl.oyl.frutossecos.data.model.ProductInputDto
import cl.oyl.frutossecos.data.repository.LocalStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class ProductForm(
    val id: String = "",
    val name: String = "",
    val category: String = "Frutos secos",
    val description: String = "",
    val presentation: String = "",
    val price: String = "",
    val stock: String = "0"
)

object ProductFormValidator {

    fun validate(
        form: ProductForm,
        existingIds: Set<String>,
        editing: Boolean
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        val id = form.id.trim()
        val price = form.price.trim().toIntOrNull()
        val stock = form.stock.trim().toIntOrNull()

        if (id.isBlank()) {
            errors["id"] = "Ingresa el identificador."
        } else if (!Regex("[A-Za-z0-9_-]+").matches(id)) {
            errors["id"] =
                "Usa letras, números, guion o guion bajo."
        } else if (
            !editing &&
            existingIds.any {
                it.equals(id, ignoreCase = true)
            }
        ) {
            errors["id"] = "Ese identificador ya existe."
        }

        if (form.name.isBlank()) {
            errors["name"] = "Ingresa el nombre."
        }

        if (form.category.isBlank()) {
            errors["category"] = "Ingresa la categoría."
        }

        if (form.description.isBlank()) {
            errors["description"] = "Ingresa la descripción."
        }

        if (form.presentation.isBlank()) {
            errors["presentation"] =
                "Ingresa la presentación, por ejemplo 250 g."
        }

        if (price == null || price <= 0) {
            errors["price"] =
                "Ingresa un precio entero mayor que cero."
        }

        if (!editing && (stock == null || stock < 0)) {
            errors["stock"] =
                "Ingresa un stock entero igual o mayor que cero."
        }

        return errors
    }
}

class ProductManagementViewModel : ViewModel() {

    private val repository
        get() = LocalStore.repository

    var products by mutableStateOf<List<LocalProduct>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var saving by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var dialogOpen by mutableStateOf(false)
        private set

    var editing by mutableStateOf(false)
        private set

    var form by mutableStateOf(ProductForm())
        private set

    var errors by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var formError by mutableStateOf("")
        private set

    init {
        refresh()
    }

    fun refresh() {
        if (loading || saving) return

        viewModelScope.launch {
            loading = true
            message = ""

            try {
                products = repository.products()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message =
                    "No se pudieron cargar los productos locales."
            } finally {
                loading = false
            }
        }
    }

    fun openCreate() {
        if (saving || loading) return

        editing = false
        form = ProductForm()
        errors = emptyMap()
        formError = ""
        dialogOpen = true
    }

    fun openEdit(product: LocalProduct) {
        if (saving || loading) return

        editing = true

        form = ProductForm(
            id = product.id,
            name = product.name,
            category = product.category,
            description = product.description.orEmpty(),
            presentation = product.presentation.orEmpty(),
            price = product.price.toString(),
            stock = product.stock.toString()
        )

        errors = emptyMap()
        formError = ""
        dialogOpen = true
    }

    fun closeDialog() {
        if (!saving) {
            dialogOpen = false
        }
    }

    fun updateField(
        field: String,
        value: String
    ) {
        if (saving) return

        form = when (field) {
            "id" -> {
                if (editing) form else form.copy(id = value)
            }

            "name" -> form.copy(name = value)
            "category" -> form.copy(category = value)
            "description" -> form.copy(description = value)
            "presentation" -> form.copy(presentation = value)
            "price" -> form.copy(price = value)

            "stock" -> {
                if (editing) form else form.copy(stock = value)
            }

            else -> form
        }

        if (errors.isNotEmpty()) {
            errors = ProductFormValidator.validate(
                form = form,
                existingIds = products.map { it.id }.toSet(),
                editing = editing
            )
        }

        formError = ""
    }

    fun save() {
        if (saving || loading) return

        errors = ProductFormValidator.validate(
            form = form,
            existingIds = products.map { it.id }.toSet(),
            editing = editing
        )

        if (errors.isNotEmpty()) return

        val submitted = form
        val wasEditing = editing

        viewModelScope.launch {
            saving = true
            formError = ""

            try {
                val latestProducts = repository.products()

                errors = ProductFormValidator.validate(
                    form = submitted,
                    existingIds = latestProducts.map { it.id }.toSet(),
                    editing = wasEditing
                )

                if (errors.isNotEmpty()) {
                    return@launch
                }

                val latestProduct = if (wasEditing) {
                    latestProducts.find {
                        it.id == submitted.id
                    } ?: error("El producto ya no existe.")
                } else {
                    null
                }

                repository.saveProduct(
                    ProductInputDto(
                        id = submitted.id.trim(),
                        name = submitted.name.trim(),
                        category = submitted.category.trim(),
                        description = submitted.description.trim(),
                        presentation = submitted.presentation.trim(),
                        price = submitted.price.trim().toInt(),
                        stock = latestProduct?.stock
                            ?: submitted.stock.trim().toInt(),
                        active = latestProduct?.active ?: true
                    )
                )

                dialogOpen = false

                message = if (wasEditing) {
                    "Producto actualizado en el celular."
                } else {
                    "Producto creado en el celular."
                }

                reloadAfterSave()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                formError =
                    "No se pudo guardar el producto local."
            } finally {
                saving = false
            }
        }
    }

    fun toggleActive(product: LocalProduct) {
        if (saving || loading) return

        viewModelScope.launch {
            saving = true
            message = ""

            try {
                val latest = repository.products().find {
                    it.id == product.id
                } ?: error("El producto ya no existe.")

                repository.saveProduct(
                    ProductInputDto(
                        id = latest.id,
                        name = latest.name,
                        category = latest.category,
                        description = latest.description.orEmpty(),
                        presentation = latest.presentation.orEmpty(),
                        price = latest.price,
                        stock = latest.stock,
                        active = !latest.active
                    )
                )

                message = if (latest.active) {
                    "Producto desactivado."
                } else {
                    "Producto activado."
                }

                reloadAfterSave()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message =
                    "No se pudo cambiar el estado del producto."
            } finally {
                saving = false
            }
        }
    }

    private suspend fun reloadAfterSave() {
        try {
            products = repository.products()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            message += " Pulsa actualizar para recargar la lista."
        }
    }
}