package cl.oyl.frutossecos.viewmodel

import cl.oyl.frutossecos.data.local.MapPointEntity
import org.junit.Assert.*
import org.junit.Test

class MapUiStateTest {
    private val store = MapPointEntity("a", "Tienda Centro", "Tienda", "Santiago", "Demo", -33.44, -70.65, "Demo")
    private val pickup = MapPointEntity("b", "Retiro Estación", "Retiro", "Santiago", "Demo", -33.45, -70.64, "Demo")

    @Test fun filtersStoresAndPickupSeparately() {
        val state = MapUiState(points = listOf(store, pickup))
        assertEquals(listOf(store), state.copy(filter = "Tiendas").visiblePoints)
        assertEquals(listOf(pickup), state.copy(filter = "Puntos de retiro").visiblePoints)
    }
    @Test fun searchIgnoresAccentsCaseAndOuterSpaces() {
        assertEquals(listOf(pickup), MapUiState(points = listOf(store, pickup), query = " ESTACION ").visiblePoints)
    }
    @Test fun hiddenSelectionFallsBackToVisiblePoint() {
        assertEquals(pickup, MapUiState(points = listOf(store, pickup), filter = "Puntos de retiro", selectedId = store.id).selected)
    }
    @Test fun noResultsMeansNoSelectedCard() {
        val state = MapUiState(points = listOf(store), query = "inexistente")
        assertTrue(state.visiblePoints.isEmpty())
        assertNull(state.selected)
    }
}
