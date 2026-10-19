package cl.oyl.frutossecos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.oyl.frutossecos.navigation.OylNavGraph
import cl.oyl.frutossecos.ui.theme.OylTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OylTheme {
                OylNavGraph()
            }
        }
    }
}
