package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import uvg.edu.tripwise.ui.components.SelectableCalendar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import java.text.SimpleDateFormat
import java.util.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(propertyId: String) {
    val context = LocalContext.current
    var property by remember { mutableStateOf<Property?>(null) }
    var unavailableDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoadingAvailability by remember { mutableStateOf(true) }

    LaunchedEffect(propertyId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoadingAvailability = true
                
                // Obtener la propiedad primero
                val fetchedProperty = RetrofitInstance.PropertyApi.getPropertyById(propertyId)
                property = fetchedProperty
                
                // Luego obtener fechas no disponibles
                val availabilityResponse = RetrofitInstance.api.getAvailability(propertyId)
                unavailableDates = availabilityResponse.unavailableDates.toSet()
                
                isLoadingAvailability = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoadingAvailability = false
            }
        }
    }

    var viajeros by remember { mutableStateOf(2) }
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var budgetForActivities by remember { mutableStateOf("") }
    var foodPercentage by remember { mutableStateOf(40f) }
    var placesPercentage by remember { mutableStateOf(40f) }
    var activitiesPercentage by remember { mutableStateOf(20f) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    
    // Función auxiliar para verificar si la distribución es válida (tolerancia de 0.5%)
    fun isBudgetDistributionValid(): Boolean {
        val total = foodPercentage + placesPercentage + activitiesPercentage
        return kotlin.math.abs(total - 100f) <= 0.5f
    }
    


    Scaffold(
        containerColor = Color.White,
        topBar = {
            SmallTopAppBar(title = {
                Text(
                    text = property?.name ?: stringResource(R.string.reservation_title),
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
            if (property == null) {
                // Mostrar indicador de carga mientras se obtiene la propiedad
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1E40AF)
                )
            } else {
                property?.let { p ->
                val maxViajeros = p.capacity ?: 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
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
                            Text(text = stringResource(R.string.location_label, p.location), fontSize = 12.sp, color = Color.Gray)
                            Text(text = stringResource(R.string.capacity_info, p.capacity), fontSize = 12.sp, color = Color.Gray)
                            Text(text = stringResource(R.string.price_per_night_info, p.pricePerNight.toString()), fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Selector de fechas con calendario
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Mostrar fechas seleccionadas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.check_in),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = if (checkInDate.isEmpty()) stringResource(R.string.select_date) else checkInDate,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (checkInDate.isEmpty()) Color.Gray else Color.Black
                                    )
                                }
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = stringResource(R.string.check_in),
                                    tint = Color(0xFF1E40AF)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.check_out),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = if (checkOutDate.isEmpty()) stringResource(R.string.select_date) else checkOutDate,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (checkOutDate.isEmpty()) Color.Gray else Color.Black
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Calendario integrado
                            SelectableCalendar(
                                unavailableDates = unavailableDates,
                                isLoading = isLoadingAvailability,
                                selectionMode = true,
                                onDateRangeSelected = { startDate, endDate ->
                                    // Convertir de "yyyy-MM-dd" a "dd/MM/yyyy"
                                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    
                                    checkInDate = outputFormat.format(inputFormat.parse(startDate) ?: Date())
                                    checkOutDate = outputFormat.format(inputFormat.parse(endDate) ?: Date())
                                }
                            )
                        }
                    }
                    
                    if (checkInDate.isNotEmpty() && checkOutDate.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.duration_days, calculateDays(checkInDate, checkOutDate)),
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Número de viajeros
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.num_travelers), fontSize = 14.sp, color = Color.Gray)
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
                            Text(stringResource(R.string.travelers_count, viajeros), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
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
                            text = stringResource(R.string.max_travelers_reached, p.capacity),
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Presupuesto para actividades
                    Text(
                        text = stringResource(R.string.budget_activities_optional),
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
                        label = { Text(stringResource(R.string.budget_additional)) },
                        leadingIcon = { Text("Q", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.budget_example)) },
                        singleLine = true
                    )
                    Text(
                        text = stringResource(R.string.budget_description),
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
                                    text = stringResource(R.string.budget_distribution_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                // Mensaje de error o información
                                val currentTotal = foodPercentage + placesPercentage + activitiesPercentage
                                val totalDiff = kotlin.math.abs(currentTotal - 100f)
                                when {
                                    currentTotal > 100.5f -> {
                                        Text(
                                            text = "⚠️ La suma excede el 100% (${String.format("%.1f", currentTotal)}%)",
                                            fontSize = 12.sp,
                                            color = Color(0xFFD32F2F),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    currentTotal < 99.5f -> {
                                        Text(
                                            text = "ℹ️ Falta ${String.format("%.1f", 100f - currentTotal)}% por distribuir",
                                            fontSize = 12.sp,
                                            color = Color(0xFFFF9800),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "✓ Distribución completa (100%)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(12.dp))

                                val totalBudget = budgetForActivities.toDoubleOrNull() ?: 0.0

                                // Comida
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${stringResource(R.string.food_emoji)} ${stringResource(R.string.food_category)}", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        stringResource(R.string.budget_percentage, String.format("%.2f", totalBudget * foodPercentage / 100), foodPercentage.toInt()),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = foodPercentage,
                                    onValueChange = { newValue ->
                                        val otherTotal = placesPercentage + activitiesPercentage
                                        // Solo permitir el cambio si no excede 100% (con tolerancia)
                                        if (newValue + otherTotal <= 100.5f) {
                                            foodPercentage = newValue
                                        }
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
                                    Text("${stringResource(R.string.places_emoji)} ${stringResource(R.string.places_category)}", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        stringResource(R.string.budget_percentage, String.format("%.2f", totalBudget * placesPercentage / 100), placesPercentage.toInt()),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = placesPercentage,
                                    onValueChange = { newValue ->
                                        val otherTotal = foodPercentage + activitiesPercentage
                                        // Solo permitir el cambio si no excede 100% (con tolerancia)
                                        if (newValue + otherTotal <= 100.5f) {
                                            placesPercentage = newValue
                                        }
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
                                    Text("${stringResource(R.string.activities_emoji)} ${stringResource(R.string.activities_category)}", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        stringResource(R.string.budget_percentage, String.format("%.2f", totalBudget * activitiesPercentage / 100), activitiesPercentage.toInt()),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Slider(
                                    value = activitiesPercentage,
                                    onValueChange = { newValue ->
                                        val otherTotal = foodPercentage + placesPercentage
                                        // Solo permitir el cambio si no excede 100% (con tolerancia)
                                        if (newValue + otherTotal <= 100.5f) {
                                            activitiesPercentage = newValue
                                        }
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
                            if (checkInDate.isNotEmpty() && checkOutDate.isNotEmpty()) {
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
                        }
                    },
                    enabled = run {
                        val hasValidDates = checkInDate.isNotEmpty() && checkOutDate.isNotEmpty()
                        val activityBudget = budgetForActivities.toDoubleOrNull() ?: 0.0
                        
                        // Si no hay presupuesto de actividades, solo validar fechas
                        // Si hay presupuesto, también validar que la distribución sea ~100% (con tolerancia)
                        hasValidDates && (activityBudget == 0.0 || isBudgetDistributionValid())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E40AF),
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                ) {
                    Text(stringResource(R.string.next), color = Color.White)
                }


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