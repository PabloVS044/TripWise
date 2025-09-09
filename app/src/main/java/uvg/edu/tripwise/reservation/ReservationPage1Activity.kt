package uvg.edu.tripwise.reservation

import androidx.compose.ui.draw.clip
import uvg.edu.tripwise.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import android.content.Intent
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.ui.platform.LocalContext
import uvg.edu.tripwise.discover.DiscoverActivity

class ReservationPage1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                ReservaScreen()
            }
        }
    }
}

@Composable
fun ReservaScreen() {
    Scaffold(
        containerColor = Color.White, // Fondo blanco total
        topBar = {
            SmallTopAppBar(
                title = { Text("Tripwise", color = Color(0xFF0066CC), fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F0F7)) {
                val context = LocalContext.current

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Search") },
                    selected = false,
                    onClick = {
                        val intent = Intent(context, DiscoverActivity::class.java)
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
                    icon = { Icon(Icons.Default.Luggage, contentDescription = "Reservacion") },
                    label = { Text("Reservacion") },
                    selected = true,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Primer card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)) // Borde gris claro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sobre tu viaje", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.hotel),
                            contentDescription = "Santorini",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Santorini, Grecia", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("5-day Paradise Island", fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ 4.9 (2700 reseñas)", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.width(12.dp))
                                Text("📅 5 días, 4 noches", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Segundo card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)) // Borde gris claro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalles del viaje", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = "15/12/2025",
                        onValueChange = {},
                        label = { Text("Fecha de check-in", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = "20/12/2025",
                        onValueChange = {},
                        label = { Text("Fecha de check-out", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    var viajeros by remember { mutableStateOf(2) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Número de viajeros", fontSize = 14.sp, color = Color.Gray) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { if (viajeros > 1) viajeros-- },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                            ) { Text("-", color = Color.White) }
                            Spacer(Modifier.width(8.dp))
                            Text("$viajeros viajeros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viajeros++ },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                            ) { Text("+", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {}
