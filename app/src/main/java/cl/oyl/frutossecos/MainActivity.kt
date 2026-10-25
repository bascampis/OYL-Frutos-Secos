package cl.oyl.frutossecos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    private var notificationIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiveNotification(intent)

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
                OylNavGraph(
                    notificationIntent = notificationIntent,
                    onNotificationConsumed = {
                        notificationIntent = null

                        intent?.removeExtra("notification_target")
                        intent?.removeExtra("notification_alias")
                        intent?.removeExtra("notification_id")
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receiveNotification(intent)
    }

    private fun receiveNotification(source: Intent?) {
        notificationIntent = source?.takeIf {
            it.hasExtra("notification_target")
        }
    }
}