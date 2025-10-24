package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class ReservationPage1Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val propertyId = intent.getStringExtra("propertyId") ?: ""

        setContent {
            TripWiseTheme {
                ReservationScreen(propertyId)
            }
        }
    }
}

@Composable
fun ReservationScreen(propertyId: String) {
    val context = LocalContext.current
    var property by remember { mutableStateOf<Property?>(null) }

    // Cargar la propiedad desde la API
    LaunchedEffect(propertyId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetchedProperty = RetrofitInstance.PropertyApi.getPropertyById(propertyId)
                property = fetchedProperty
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Estado para número de viajeros
    var viajeros by remember { mutableStateOf(2) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SmallTopAppBar(title = {
                Text(
                    text = property?.name ?: "Reserva",
                    color = Color(0xFF0066CC),
                    fontWeight = FontWeight.Bold
                )
            })
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F0F7)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
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
                    icon = { Icon(Icons.Default.Luggage, contentDescription = "Reservas") },
                    label = { Text("Reservas") },
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
                    label = { Text("Perfil") },
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

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            property?.let { p ->
                val maxViajeros = p.capacity ?: 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {

                    // Card con información de la propiedad
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = p.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = p.pictures.firstOrNull(),
                                contentDescription = p.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                error = painterResource(android.R.drawable.ic_menu_gallery)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(text = p.description, fontSize = 14.sp, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text(text = "Ubicación: ${p.location}", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Capacidad: ${p.capacity}", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Precio por noche: Q${p.pricePerNight}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Fechas
                    Column {
                        OutlinedTextField(
                            value = "15/12/2025",
                            onValueChange = {},
                            label = { Text("Check-in") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = "20/12/2025",
                            onValueChange = {},
                            label = { Text("Check-out") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Número de viajeros
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Número de viajeros", fontSize = 14.sp, color = Color.Gray)
                    }
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
                                onClick = {
                                    if (viajeros < maxViajeros) viajeros++
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                            ) { Text("+", color = Color.White) }
                        }
                    }

                    if (viajeros == maxViajeros) {
                        Text(
                            text = "Has alcanzado el número máximo de viajeros (${p.capacity})",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Botón siguiente
                Button(
                    onClick = {
                        val intent = Intent(context, ReservationPage2Activity::class.java)
                        intent.putExtra("propertyId", p.id)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                ) {
                    Text("Siguiente", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.White)
    )
}

@Preview(showBackground = true)
@Composable
fun ReservationScreenPreview() {
    TripWiseTheme {
        ReservationScreen("")
    }
}
