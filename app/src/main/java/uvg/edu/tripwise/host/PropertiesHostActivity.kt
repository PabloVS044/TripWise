package uvg.edu.tripwise.host

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.host.PropertiesHost
import uvg.edu.tripwise.auth.LoginActivity   // ← ruta correcta

class PropertiesHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                PropertiesHost(
                    onLogout = { handleLogout() }   // botón de cerrar sesión
                )
            }
        }
    }

    private fun handleLogout() {
        // Limpia sesión local (ajusta si usan otra storage)
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Volver a Login limpiando el backstack
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        // Alternativa si prefieres no usar apply():
        // val intent = Intent(this, LoginActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
