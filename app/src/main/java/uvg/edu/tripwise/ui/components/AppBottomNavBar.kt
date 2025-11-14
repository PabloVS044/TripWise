package uvg.edu.tripwise.ui.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import uvg.edu.tripwise.R
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.profile.ProfileActivity
import uvg.edu.tripwise.reservation.MyReservationsActivity

@Composable
fun AppBottomNavBar(currentScreen: String) {
    val context = LocalContext.current

    NavigationBar(containerColor = Color(0xFFF7F0F7)) {
        // --- Item de Búsqueda (Discover) ---
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_desc)) },
            label = { Text(stringResource(R.string.nav_search)) },
            selected = currentScreen == "Discover",
            onClick = {
                val intent = Intent(context, DiscoverActivity::class.java)
                // Flags para evitar crear nuevas instancias si ya está abierta
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Luggage, contentDescription = stringResource(R.string.reservation_desc)) },
            label = { Text(stringResource(R.string.nav_reservation)) },
            selected = currentScreen == "Reservation",
            onClick = {
                val intent = Intent(context, MyReservationsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // --- Item de Perfil ---
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile_desc)) },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = currentScreen == "Profile",
            onClick = {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}