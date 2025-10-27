package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.ui.components.AppBottomNavBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class ReservationPage2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val propertyId = intent.getStringExtra("propertyId") ?: ""
        val numTravelers = intent.getIntExtra("numTravelers", 1)
        val checkInDate = intent.getStringExtra("checkInDate") ?: ""
        val checkOutDate = intent.getStringExtra("checkOutDate") ?: ""
        val days = intent.getIntExtra("days", 1)
        val payment = intent.getDoubleExtra("payment", 0.0)

        setContent {
            TripWiseTheme {
                ReservationPage2Screen(
                    propertyId = propertyId,
                    numTravelers = numTravelers,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    days = days,
                    payment = payment
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationPage2Screen(
    propertyId: String,
    numTravelers: Int,
    checkInDate: String,
    checkOutDate: String,
    days: Int,
    payment: Double
) {
    val context = LocalContext.current
    var property by remember { mutableStateOf<Property?>(null) }

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

    var selectedRoom by remember { mutableStateOf("standard") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        property?.name ?: "Reserva",
                        color = Color(0xFF0066CC),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, ReservationPage1Activity::class.java)
                        intent.putExtra("propertyId", propertyId)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1E40AF))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                BottomAppBar(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E40AF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔙 Botón Atrás
                        TextButton(
                            onClick = {
                                val intent = Intent(context, ReservationPage1Activity::class.java)
                                intent.putExtra("propertyId", propertyId)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E40AF))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF1E40AF))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atrás")
                        }

                        Button(
                            onClick = {
                                Log.d("DEBUG", "Going to ReservationPage3Activity with $numTravelers travelers for propertyId $propertyId")
                                val intent = Intent(context, ReservationPage3Activity::class.java)
                                intent.putExtra("propertyId", propertyId)
                                intent.putExtra("numTravelers", numTravelers)
                                intent.putExtra("checkInDate", checkInDate)
                                intent.putExtra("checkOutDate", checkOutDate)
                                intent.putExtra("days", days)
                                intent.putExtra("payment", payment)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                        ) {
                            Text("Siguiente", color = Color.White)
                        }
                    }
                }

                //  Barra inferior de navegación
                AppBottomNavBar(currentScreen = "Reservation")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            property?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Selecciona tipo de habitación en ${p.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Información de la propiedad
                    Text("Ubicación: ${p.location}", fontSize = 14.sp, color = Color.Gray)
                    Text("Capacidad: ${p.capacity}", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        "Precio por noche: Q${p.pricePerNight}",
                        fontSize = 14.sp,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tarjeta con la habitación
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (selectedRoom == "standard") Color(0xFF1E40AF) else Color.LightGray
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedRoom == "standard",
                                    onClick = { selectedRoom = "standard" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF1E40AF),
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                    Text(
                                        "Q${p.pricePerNight} por noche",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E40AF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(p.description, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1E40AF)
                )
            }
        }
    }
}
