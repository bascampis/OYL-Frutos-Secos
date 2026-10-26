package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.local.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class PromotionForm(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val productId: String = "",
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = LocalDate.now().plusDays(7).toString(),
    val active: Boolean = true
)

object PromotionValidator {

    fun validate(
        form: PromotionForm,
        products: List<ProductEntity>
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (form.title.isBlank()) {
            errors["title"] = "Ingresa un título."
        }

        if (form.description.isBlank()) {
            errors["description"] = "Ingresa una descripción."
        }

        if (products.none { it.id == form.productId }) {
            errors["productId"] = "Selecciona un producto."
        } else if (
            form.active &&
            products.none { it.id == form.productId && it.active }
        ) {
            errors["productId"] =
                "Selecciona un producto activo para publicar."
        }

        val start = runCatching {
            LocalDate.parse(form.startDate.trim())
        }.getOrNull()

        val end = runCatching {
            LocalDate.parse(form.endDate.trim())
        }.getOrNull()

        if (start == null) {
            errors["startDate"] = "Usa el formato AAAA-MM-DD."
        }

        if (end == null) {
            errors["endDate"] = "Usa el formato AAAA-MM-DD."
        } else if (start != null && end.isBefore(start)) {
            errors["endDate"] =
                "La fecha final no puede ser anterior al inicio."
        }

        return errors
    }
}

class PromotionsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as OylApplication

    private val dao = PromotionsDatabase
        .get(application)
        .promotionDao()

    private fun isAdmin(): Boolean {
        return app.getSharedPreferences("oyl_session", 0)
            .getString("role", "") == "ADMINISTRADOR"
    }

    var promotions by mutableStateOf<List<PromotionEntity>>(emptyList())
        private set

    var products by mutableStateOf<List<ProductEntity>>(emptyList())
        private set

    var form by mutableStateOf(PromotionForm())
        private set

    var errors by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var dialogOpen by mutableStateOf(false)
        private set

    var loading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var formError by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            try {
                app.catalogRepository.seedDemoProducts()

                combine(
                    dao.observe(),
                    app.database.catalogDao().allProducts()
                ) { offers, items ->
                    offers to items
                }.collect { (offers, items) ->
                    promotions = offers
                    products = items
                    loading = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                loading = false
                message = "No se pudieron cargar las promociones."
            }
        }
    }

    fun openCreate() {
        if (!isAdmin() || saving) return

        form = PromotionForm()
        errors = emptyMap()
        formError = ""
        dialogOpen = true
    }

    fun openEdit(value: PromotionEntity) {
        if (!isAdmin() || saving) return

        form = PromotionForm(
            id = value.id,
            title = value.title,
            description = value.description,
            productId = value.productId,
            startDate = value.startDate,
            endDate = value.endDate,
            active = value.active
        )

        errors = emptyMap()
        formError = ""
        dialogOpen = true
    }

    fun closeDialog() {
        if (!saving) dialogOpen = false
    }

    fun update(value: PromotionForm) {
        if (saving) return

        form = value
        formError = ""

        if (errors.isNotEmpty()) {
            errors = PromotionValidator.validate(form, products)
        }
    }

    fun save() {
        if (!isAdmin() || saving) return

        errors = PromotionValidator.validate(form, products)
        if (errors.isNotEmpty()) return

        val submitted = form
        saving = true

        viewModelScope.launch {
            try {
                val latestProducts =
                    app.database.catalogDao().productList()

                errors = PromotionValidator.validate(
                    submitted,
                    latestProducts
                )

                if (errors.isNotEmpty()) return@launch

                dao.save(
                    PromotionEntity(
                        id = submitted.id.ifBlank {
                            UUID.randomUUID().toString()
                        },
                        title = submitted.title.trim(),
                        description = submitted.description.trim(),
                        productId = submitted.productId,
                        startDate = submitted.startDate.trim(),
                        endDate = submitted.endDate.trim(),
                        active = submitted.active
                    )
                )

                dialogOpen = false
                message = "Promoción guardada."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                formError = "No se pudo guardar la promoción."
            } finally {
                saving = false
            }
        }
    }
}