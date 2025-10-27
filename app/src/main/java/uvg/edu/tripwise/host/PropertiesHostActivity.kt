package uvg.edu.tripwise.host

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uvg.edu.tripwise.auth.LoginActivity
import uvg.edu.tripwise.ui.theme.TripWiseTheme

object SessionKeys {
    const val PREFS = "auth"
    const val USER_ID = "USER_ID"
    const val LEGACY_USER_ID = "user_id"
}

class PropertiesHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SessionKeys.PREFS, MODE_PRIVATE)
        val currentUserId = prefs.getString(SessionKeys.USER_ID, prefs.getString(SessionKeys.LEGACY_USER_ID, null))
        if (currentUserId.isNullOrBlank()) {
            handleLogout()
            return
        }

        val propertyId: String? = intent?.getStringExtra(EXTRA_PROPERTY_ID)

        setContent {
            TripWiseTheme {
                PropertiesHost(
                    propertyId = propertyId,
                    onLogout = { handleLogout() }
                )
            }
        }
    }

    private fun handleLogout() {
        getSharedPreferences(SessionKeys.PREFS, MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_PROPERTY_ID = "extra_property_id"

        fun launch(
            context: Context,
            propertyId: String? = null,
            clearTask: Boolean = false
        ) {
            val i = Intent(context, PropertiesHostActivity::class.java).apply {
                if (propertyId != null) putExtra(EXTRA_PROPERTY_ID, propertyId)
                if (clearTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(i)
        }
    }
}