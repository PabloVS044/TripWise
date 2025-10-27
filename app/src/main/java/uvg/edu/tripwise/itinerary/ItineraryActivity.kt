package uvg.edu.tripwise.itinerary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.network.ItineraryResponse
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class ItineraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val reservationId = intent.getStringExtra("reservationId")
        val itineraryId = intent.getStringExtra("itineraryId")
        
        setContent {
            TripWiseTheme {
                ItineraryScreen(
                    reservationId = reservationId,
                    itineraryId = itineraryId,
                    onBackPressed = {
                        finish()
                    },
                    onLogout = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

data class ItineraryItem(
    val type: ItemType,
    val name: String,
    val day: Int,
    val schedule: String
)

enum class ItemType {
    RESTAURANT, TOURISTIC_PLACE, ACTIVITY, SCHEDULE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    reservationId: String?,
    itineraryId: String?,
    onBackPressed: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var itinerary by remember { mutableStateOf<ItineraryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reservationId, itineraryId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetchedItinerary = when {
                    !itineraryId.isNullOrEmpty() -> {
                        RetrofitInstance.api.getItineraryById(itineraryId)
                    }
                    !reservationId.isNullOrEmpty() -> {
                        RetrofitInstance.api.getItineraryByReservation(reservationId)
                    }
                    else -> null
                }

                withContext(Dispatchers.Main) {
                    if (fetchedItinerary != null) {
                        itinerary = fetchedItinerary
                    } else {
                        errorMessage = "No se encontró el itinerario"
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error al cargar el itinerario: ${e.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            }
        }
    }

    fun mapItineraryToItems(itinerary: ItineraryResponse): List<ItineraryItem> {
        val items = mutableListOf<ItineraryItem>()
        
        for (i in itinerary.schedules.indices) {
            val day = itinerary.days.getOrNull(i) ?: 1
            
            items.add(ItineraryItem(ItemType.SCHEDULE, itinerary.schedules[i], day, ""))
            
            if (i < itinerary.restaurants.size) {
                items.add(ItineraryItem(ItemType.RESTAURANT, itinerary.restaurants[i], day, itinerary.schedules[i]))
            }
            if (i < itinerary.touristicPlaces.size) {
                items.add(ItineraryItem(ItemType.TOURISTIC_PLACE, itinerary.touristicPlaces[i], day, itinerary.schedules[i]))
            }
            if (i < itinerary.activities.size) {
                items.add(ItineraryItem(ItemType.ACTIVITY, itinerary.activities[i], day, itinerary.schedules[i]))
            }
        }
        
        return items
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF7C3AED)
            )
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage ?: "Error desconocido",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Regresar", color = Color.White)
                }
            }
        } else {
            val itineraryData = itinerary
            if (itineraryData != null) {
                val itineraryItems = mapItineraryToItems(itineraryData)
                val itineraryByDays = itineraryItems.groupBy { it.day }.toSortedMap()
                
                Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar personalizado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Mi Itinerario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            }

            // Contenido del itinerario
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TripInfoCard(itineraryData)
                }

                itineraryByDays.forEach { (day, items) ->
                    item {
                        DayHeader(day = day)
                    }

                    items(items) { item ->
                        when (item.type) {
                            ItemType.RESTAURANT -> RestaurantCard(item)
                            ItemType.TOURISTIC_PLACE -> TouristicPlaceCard(item)
                            ItemType.ACTIVITY -> ActivityCard(item)
                            ItemType.SCHEDULE -> ScheduleCard(item)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    ActionButtons()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
                }
            }
        }
    }
}

@Composable
fun TripInfoCard(itinerary: ItineraryResponse) {
    val maxDay = itinerary.days.maxOrNull() ?: 1
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Viaje",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("📅 Duración", "$maxDay días")
                InfoItem("🍽️ Restaurantes", "${itinerary.restaurants.size}")
                InfoItem("� Lugares", "${itinerary.touristicPlaces.size}")
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
fun DayHeader(day: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "Día $day",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
fun RestaurantCard(item: ItineraryItem) {
    ItineraryItemCard(
        item = item,
        icon = Icons.Default.Restaurant,
        backgroundColor = Color(0xFFFFE0B2),
        iconTint = Color(0xFFE65100)
    )
}

@Composable
fun TouristicPlaceCard(item: ItineraryItem) {
    ItineraryItemCard(
        item = item,
        icon = Icons.Default.Place,
        backgroundColor = Color(0xFFE8F5E8),
        iconTint = Color(0xFF2E7D32)
    )
}

@Composable
fun ActivityCard(item: ItineraryItem) {
    ItineraryItemCard(
        item = item,
        icon = Icons.Default.DirectionsRun,
        backgroundColor = Color(0xFFE3F2FD),
        iconTint = Color(0xFF1565C0)
    )
}

@Composable
fun ScheduleCard(item: ItineraryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ItineraryItemCard(
    item: ItineraryItem,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.87f)
                )
                Text(
                    text = item.schedule,
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ActionButtons() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* Regenerar itinerario */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Regenerar Itinerario", color = Color.White)
        }

        OutlinedButton(
            onClick = { /* Ver recomendaciones */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF7C3AED)
            )
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Recomendaciones")
        }

        OutlinedButton(
            onClick = { /* Personalizar itinerario */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF7C3AED)
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Personalizar")
        }
    }
}