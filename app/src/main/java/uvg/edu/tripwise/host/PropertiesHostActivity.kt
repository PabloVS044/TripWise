package uvg.edu.tripwise.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class PropertiesHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                PropertiesHost()
            }
        }
    }
}

