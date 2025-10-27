package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.withContext
import uvg.edu.tripwise.auth.SessionManager
import uvg.edu.tripwise.itinerary.ItineraryActivity
import uvg.edu.tripwise.network.CreateReservationRequest
import uvg.edu.tripwise.network.RetrofitInstance
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
    val scrollState = rememberScrollState()
    val sessionManager = remember { SessionManager(context) }

    val names = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val lastNames = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val emails = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val phones = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loadingMessage by remember { mutableStateOf("Procesando...") }

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
                title = { Text("Traveler Information", color = Color.White) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF1E40AF))
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(context, ReservationPage2Activity::class.java)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E40AF))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E40AF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }

                    Button(
                        onClick = {
                            val allFieldsFilled = names.all { it.isNotBlank() } &&
                                    lastNames.all { it.isNotBlank() } &&
                                    emails.all { it.isNotBlank() } &&
                                    phones.all { it.isNotBlank() }

                            if (!allFieldsFilled) {
                                Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
                            if (!emails.all { emailPattern.matches(it) }) {
                                Toast.makeText(context, "Por favor ingrese emails válidos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

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
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Confirm Reservation", color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Traveler Details",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Formulario dinámico para cada viajero
            repeat(numTravelers) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Traveler ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E40AF)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = names[index],
                            onValueChange = { names[index] = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = lastNames[index],
                            onValueChange = { lastNames[index] = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emails[index],
                            onValueChange = { emails[index] = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phones[index],
                            onValueChange = { phones[index] = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
