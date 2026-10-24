package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.local.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

data class MapUiState(
    val points: List<MapPointEntity> = emptyList(),
    val query: String = "",
    val filter: String = "Todas",
    val selectedId: String? = null,
    val loading: Boolean = true,
    val error: String? = null
) {
    val visiblePoints: List<MapPointEntity> get() = points.filter {
        val categoryMatches = filter == "Todas" ||
            (filter == "Tiendas" && it.type == "Tienda") ||
            (filter == "Puntos de retiro" && it.type == "Retiro")
        categoryMatches && normalized("${it.name} ${it.address}").contains(normalized(query.trim()))
    }
    val selected: MapPointEntity? get() = visiblePoints.firstOrNull { it.id == selectedId }
        ?: visiblePoints.firstOrNull()
}

private fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(java.util.Locale.ROOT)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MapPointsRepository(MapPointsDatabase.get(application))
    private val _state = MutableStateFlow(MapUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedDemoPoints()
                repository.points.collect { points ->
                    _state.update { it.copy(points = points, loading = false, error = null) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = "No se pudieron cargar los puntos locales.") }
            }
        }
    }

    fun search(value: String) = _state.update { it.copy(query = value, selectedId = null) }
    fun filter(value: String) = _state.update { it.copy(filter = value, selectedId = null) }
    fun select(id: String) = _state.update { it.copy(selectedId = id) }
}
