package cl.oyl.frutossecos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Reemplaza Dispatchers.Main para poder probar ViewModels fuera de Android. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** Espera (máx. 3 s) a que una condición se cumpla; útil cuando el ViewModel usa Dispatchers.IO. */
fun awaitTrue(timeoutMs: Long = 3000, condition: () -> Boolean) {
    val end = System.currentTimeMillis() + timeoutMs
    while (!condition() && System.currentTimeMillis() < end) Thread.sleep(10)
}
