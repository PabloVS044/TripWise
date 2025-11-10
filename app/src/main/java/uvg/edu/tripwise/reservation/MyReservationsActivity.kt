package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.tripwise.auth.SessionManager
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.itinerary.ItineraryActivity
import uvg.edu.tripwise.network.ReservationResponse
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.ui.components.AppBottomNavBar
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import java.text.SimpleDateFormat
import java.util.*

class MyReservationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                MyReservationsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var reservations by remember { mutableStateOf<List<ReservationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val userId = sessionManager.getUserId()
        if (userId.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Sesión expirada", Toast.LENGTH_LONG).show()
                errorMessage = "Sesión expirada"
                isLoading = false
            }
            return@LaunchedEffect
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getReservationsByUser(userId)
                withContext(Dispatchers.Main) {
                    reservations = response
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error al cargar reservas: ${e.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LogoAppTopBar(onLogout = {})
        },
        bottomBar = {
            AppBottomNavBar(currentScreen = "Reservation")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF1E88E5)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.Red
                        )
                    }
                }
                reservations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes reservas aún",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Mis Reservas",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(reservations) { reservation ->
                            ReservationCard(
                                reservation = reservation,
                                onClick = {
                                    if (reservation.itinerary != null) {
                                        val intent = Intent(context, ItineraryActivity::class.java)
                                        intent.putExtra("reservationId", reservation.id)
                                        intent.putExtra("itineraryId", reservation.itinerary.id)
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Esta reserva no tiene itinerario", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: ReservationResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.propertyBooked.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reservation.propertyBooked.location,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                StateChip(state = reservation.state)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (reservation.propertyBooked.pictures.isNotEmpty()) {
                AsyncImage(
                    model = reservation.propertyBooked.pictures.firstOrNull(),
                    contentDescription = reservation.propertyBooked.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.CalendarToday,
                    text = formatDate(reservation.checkInDate)
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                InfoChip(
                    icon = Icons.Default.CalendarToday,
                    text = formatDate(reservation.checkOutDate)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.People,
                    text = "${reservation.persons} ${if (reservation.persons == 1) "persona" else "personas"}"
                )
                InfoChip(
                    icon = Icons.Default.NightsStay,
                    text = "${reservation.days} ${if (reservation.days == 1) "noche" else "noches"}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${String.format("%.2f", reservation.payment)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E88E5)
                )

                if (reservation.itinerary != null) {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        )
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Itinerario")
                    }
                }
            }
        }
    }
}

@Composable
fun StateChip(state: String) {
    val (backgroundColor, textColor, text) = when (state) {
        "confirmed" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Confirmada")
        "pending" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Pendiente")
        "cancelled" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelada")
        else -> Triple(Color.LightGray, Color.DarkGray, state)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}
