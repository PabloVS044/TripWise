package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.itinerary.ItineraryActivity
import uvg.edu.tripwise.network.CreateReservationRequest
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.ui.components.AppBottomNavBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import java.text.SimpleDateFormat
import java.util.*

class ReservationPage3Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val numTravelers = intent.getIntExtra("numTravelers", 1)
        val propertyId = intent.getStringExtra("propertyId") ?: ""
        val checkInDate = intent.getStringExtra("checkInDate") ?: ""
        val checkOutDate = intent.getStringExtra("checkOutDate") ?: ""
        val days = intent.getIntExtra("days", 1)
        val payment = intent.getDoubleExtra("payment", 0.0)

        Log.d("ReservationPage3", "Received data - propertyId: $propertyId, travelers: $numTravelers, checkIn: $checkInDate, checkOut: $checkOutDate, days: $days, payment: $payment")

        setContent {
            TripWiseTheme {
                ReservationPage3Screen(
                    numTravelers = numTravelers,
                    propertyId = propertyId,
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
fun ReservationPage3Screen(
    numTravelers: Int,
    propertyId: String,
    checkInDate: String,
    checkOutDate: String,
    days: Int,
    payment: Double
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var property by remember { mutableStateOf<Property?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loadingMessage by remember { mutableStateOf("Procesando...") }

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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color(0xFF1E40AF),
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = loadingMessage,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Por favor espera...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Reserva", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, ReservationPage1Activity::class.java)
                        intent.putExtra("propertyId", propertyId)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF1E40AF))
            )
        },
        bottomBar = {
            AppBottomNavBar(currentScreen = "Reservation")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Resumen de Reserva",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            // Tarjeta con la información de la propiedad
            property?.let { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = p.pictures.firstOrNull(),
                            contentDescription = p.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = p.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = p.location,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Detalles de la reserva
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Check-in:", fontWeight = FontWeight.Medium)
                        Text(checkInDate, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Check-out:", fontWeight = FontWeight.Medium)
                        Text(checkOutDate, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Días:", fontWeight = FontWeight.Medium)
                        Text("$days", color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Viajeros:", fontWeight = FontWeight.Medium)
                        Text("$numTravelers", color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Q${String.format("%.2f", payment)}", 
                            color = Color(0xFF1E40AF), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Botón de confirmar reserva
            Button(
                onClick = {
                    val userId = sessionManager.getUserId()
                    if (userId.isNullOrEmpty()) {
                        Toast.makeText(context, "Sesión expirada. Por favor inicie sesión nuevamente", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    loadingMessage = "Creando reserva..."

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(Dispatchers.Main) {
                                loadingMessage = "Validando disponibilidad..."
                            }
                            
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                            
                            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val checkInParsed = inputFormat.parse(checkInDate)
                            val checkOutParsed = inputFormat.parse(checkOutDate)
                            
                            if (checkInParsed == null || checkOutParsed == null) {
                                throw Exception("Fechas inválidas")
                            }
                            
                            val checkInFormatted = dateFormat.format(checkInParsed)
                            val checkOutFormatted = dateFormat.format(checkOutParsed)

                            val reservationRequest = CreateReservationRequest(
                                reservationUser = userId,
                                propertyBooked = propertyId,
                                checkInDate = checkInFormatted,
                                checkOutDate = checkOutFormatted,
                                payment = payment,
                                persons = numTravelers,
                                days = days
                            )

                            withContext(Dispatchers.Main) {
                                loadingMessage = "Confirmando reserva..."
                            }

                            val response = RetrofitInstance.api.createReservation(reservationRequest)

                            withContext(Dispatchers.Main) {
                                loadingMessage = "Generando tu itinerario personalizado..."
                            }

                            kotlinx.coroutines.delay(2000)

                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful && response.body() != null) {
                                    val responseBody = response.body()!!
                                    val reservation = responseBody.reservation
                                    val itinerary = responseBody.itinerary
                                    
                                    Toast.makeText(context, "¡Reserva creada exitosamente!", Toast.LENGTH_SHORT).show()
                                    
                                    val intent = Intent(context, ItineraryActivity::class.java)
                                    intent.putExtra("reservationId", reservation.id)
                                    intent.putExtra("itineraryId", itinerary?.id)
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    errorMessage = "Error al crear la reserva: ${errorBody ?: "Error desconocido"}"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error: ${e.message}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Confirmar Reserva", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
