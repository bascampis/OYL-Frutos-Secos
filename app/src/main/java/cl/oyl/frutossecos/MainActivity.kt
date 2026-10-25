package cl.oyl.frutossecos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cl.oyl.frutossecos.data.local.DemoNotificationStore
import cl.oyl.frutossecos.navigation.OylNavGraph
import cl.oyl.frutossecos.ui.theme.OylTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notices = DemoNotificationStore(applicationContext)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    notices.refresh()
                    delay(15_000)
                }
            }
        }

        setContent {
            OylTheme {
                OylNavGraph()
            }
        }
    }
}