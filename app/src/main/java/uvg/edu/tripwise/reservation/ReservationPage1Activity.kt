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
import androidx.compose.ui.res.stringResource
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
import uvg.edu.tripwise.ui.components.AppBottomNavBar
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

    var viajeros by remember { mutableStateOf(2) }
    var checkInDate by remember { mutableStateOf("15/12/2025") }
    var checkOutDate by remember { mutableStateOf("20/12/2025") }
    var budgetForActivities by remember { mutableStateOf("") }
    var foodPercentage by remember { mutableStateOf(40f) }
    var placesPercentage by remember { mutableStateOf(40f) }
    var activitiesPercentage by remember { mutableStateOf(20f) }

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
            AppBottomNavBar(currentScreen = "Reservation")
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

                    Spacer(Modifier.height(12.dp))

                    Column {
                        OutlinedTextField(
                            value = checkInDate,
                            onValueChange = { checkInDate = it },
                            label = { Text("Check-in") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = checkOutDate,
                            onValueChange = { checkOutDate = it },
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

                    Spacer(Modifier.height(16.dp))

                    // Presupuesto para actividades
                    Text(
                        text = "Presupuesto para Actividades (Opcional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = budgetForActivities,
                        onValueChange = { 
                            // Solo permitir números
                            if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                                budgetForActivities = it
                            }
                        },
                        label = { Text("Presupuesto adicional (Q)") },
                        leadingIcon = { Text("Q", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 500") },
                        singleLine = true
                    )
                    Text(
                        text = "Este presupuesto es adicional al costo de la reservación y se usará para comida, lugares turísticos y actividades.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Mostrar distribución solo si hay presupuesto
                    if (budgetForActivities.isNotEmpty() && budgetForActivities.toDoubleOrNull() != null && budgetForActivities.toDouble() > 0) {
                        Spacer(Modifier.height(16.dp))
                        
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Distribución del Presupuesto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(12.dp))

                                val totalBudget = budgetForActivities.toDoubleOrNull() ?: 0.0

                                // Comida
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🍽️ Comida", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        "Q${String.format("%.2f", totalBudget * foodPercentage / 100)} (${foodPercentage.toInt()}%)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = foodPercentage,
                                    onValueChange = { 
                                        foodPercentage = it
                                        // Ajustar otros porcentajes proporcionalmente
                                        val remaining = 100f - foodPercentage
                                        val ratio = placesPercentage / (placesPercentage + activitiesPercentage)
                                        placesPercentage = remaining * ratio
                                        activitiesPercentage = remaining * (1 - ratio)
                                    },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF1E40AF),
                                        activeTrackColor = Color(0xFF1E40AF)
                                    )
                                )

                                Spacer(Modifier.height(8.dp))

                                // Lugares Turísticos
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍 Lugares Turísticos", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        "Q${String.format("%.2f", totalBudget * placesPercentage / 100)} (${placesPercentage.toInt()}%)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = placesPercentage,
                                    onValueChange = { 
                                        placesPercentage = it
                                        val remaining = 100f - placesPercentage
                                        val ratio = foodPercentage / (foodPercentage + activitiesPercentage)
                                        foodPercentage = remaining * ratio
                                        activitiesPercentage = remaining * (1 - ratio)
                                    },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF1E40AF),
                                        activeTrackColor = Color(0xFF1E40AF)
                                    )
                                )

                                Spacer(Modifier.height(8.dp))

                                // Actividades
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎯 Actividades", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        "Q${String.format("%.2f", totalBudget * activitiesPercentage / 100)} (${activitiesPercentage.toInt()}%)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = activitiesPercentage,
                                    onValueChange = { 
                                        activitiesPercentage = it
                                        val remaining = 100f - activitiesPercentage
                                        val ratio = foodPercentage / (foodPercentage + placesPercentage)
                                        foodPercentage = remaining * ratio
                                        placesPercentage = remaining * (1 - ratio)
                                    },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF1E40AF),
                                        activeTrackColor = Color(0xFF1E40AF)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }

                // Botón siguiente
                Button(
                    onClick = {
                        property?.let { p ->
                            val days = calculateDays(checkInDate, checkOutDate)
                            val totalPayment = p.pricePerNight * days
                            val activityBudget = budgetForActivities.toDoubleOrNull() ?: 0.0

                            val intent = Intent(context, ReservationPage3Activity::class.java)
                            intent.putExtra("propertyId", p.id)
                            intent.putExtra("numTravelers", viajeros)
                            intent.putExtra("checkInDate", checkInDate)
                            intent.putExtra("checkOutDate", checkOutDate)
                            intent.putExtra("days", days)
                            intent.putExtra("payment", totalPayment)
                            intent.putExtra("activityBudget", activityBudget)
                            intent.putExtra("foodPercentage", foodPercentage)
                            intent.putExtra("placesPercentage", placesPercentage)
                            intent.putExtra("activitiesPercentage", activitiesPercentage)
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
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

fun calculateDays(checkIn: String, checkOut: String): Int {
    return try {
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val startDate = format.parse(checkIn)
        val endDate = format.parse(checkOut)
        val diff = endDate.time - startDate.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        1
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