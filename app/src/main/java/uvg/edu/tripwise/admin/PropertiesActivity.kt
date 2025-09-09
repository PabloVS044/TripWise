package uvg.edu.tripwise.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.tripwise.data.model.Post
import uvg.edu.tripwise.data.repository.propertyRepository
import uvg.edu.tripwise.ui.screens.PropertiesScreen
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class PropertiesActivity : ComponentActivity() {
    private val repository = propertyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                PropertiesScreen(repository)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PropertiesScreenPreview() {
    TripWiseTheme {
        PropertiesScreen(propertyRepository())
    }
}