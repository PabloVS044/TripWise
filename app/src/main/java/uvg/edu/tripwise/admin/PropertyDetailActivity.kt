package uvg.edu.tripwise.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.network.UpdatePropertyRequest

class PropertyDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val propertyId = intent.getStringExtra("propertyId") ?: ""
        setContent {
            TripWiseTheme {
                PropertyDetailScreen(propertyId = propertyId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(propertyId: String, onBack: () -> Unit) {
    var property by remember { mutableStateOf<Property?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val propertyRepository = remember { PropertyRepository() }

    // Estados para los campos editables
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("") }
    var pricePerNight by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var approved by remember { mutableStateOf("") }

    LaunchedEffect(propertyId) {
        scope.launch {
            try {
                isLoading = true
                val prop = propertyRepository.getPropertyById(propertyId)
                property = prop

                name = prop.name
                location = prop.location
                propertyType = prop.propertyType
                pricePerNight = prop.pricePerNight.toString()
                capacity = prop.capacity.toString()
                approved = prop.approved
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la propiedad", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun onSaveChanges() {
        val currentProperty = property ?: return

        scope.launch {
            isSaving = true
            try {
                val request = UpdatePropertyRequest(
                    name = name,
                    description = currentProperty.description,
                    location = location,
                    pricePerNight = pricePerNight.toDoubleOrNull() ?: currentProperty.pricePerNight,
                    capacity = capacity.toIntOrNull() ?: currentProperty.capacity,
                    pictures = currentProperty.pictures,
                    amenities = currentProperty.amenities,
                    propertyType = propertyType, // <-- Se usan los estados actualizados
                    approved = approved, // <-- Se usan los estados actualizados
                    latitude = currentProperty.latitude,
                    longitude = currentProperty.longitude
                )

                propertyRepository.updateProperty(currentProperty.id, request)
                Toast.makeText(context, "Propiedad actualizada", Toast.LENGTH_SHORT).show()
                onBack()

            } catch (e: Exception) {
                Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Property Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (property == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Property not found", color = Color.Gray)
            }
        } else {
            PropertyDetailContent(
                modifier = Modifier.padding(innerPadding),
                propertyId = property!!.id,
                name = name,
                onNameChange = { name = it },
                location = location,
                onLocationChange = { location = it },
                propertyType = propertyType,
                onPropertyTypeChange = { propertyType = it }, // <-- CAMBIO: Pasamos el setter
                pricePerNight = pricePerNight,
                onPricePerNightChange = { pricePerNight = it },
                capacity = capacity,
                onCapacityChange = { capacity = it },
                approved = approved,
                onApprovedChange = { approved = it }, // <-- CAMBIO: Pasamos el setter
                isSaving = isSaving,
                onSaveClick = { onSaveChanges() }
            )
        }
    }
}

@Composable
fun PropertyDetailContent(
    modifier: Modifier = Modifier,
    propertyId: String,
    name: String, onNameChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    propertyType: String, onPropertyTypeChange: (String) -> Unit, // <-- CAMBIO
    pricePerNight: String, onPricePerNightChange: (String) -> Unit,
    capacity: String, onCapacityChange: (String) -> Unit,
    approved: String, onApprovedChange: (String) -> Unit, // <-- CAMBIO
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    // Opciones basadas en tu property.model.js
    val typeOptions = listOf("house", "apartment", "villa", "cottage", "hotel")
    val statusOptions = listOf("pending", "approved", "rejected")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Property ID", value = propertyId)
            }
        }

        Text(
            text = "Edit Property",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        // Campos Editables
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        // --- CAMBIO: De TextField a Dropdown ---
        EnumDropdownSelector(
            label = "Type",
            options = typeOptions,
            selectedOption = propertyType,
            onOptionSelected = onPropertyTypeChange,
            enabled = !isSaving
        )
        // --- FIN DEL CAMBIO ---

        OutlinedTextField(
            value = pricePerNight,
            onValueChange = onPricePerNightChange,
            label = { Text("Price per Night") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        OutlinedTextField(
            value = capacity,
            onValueChange = onCapacityChange,
            label = { Text("Capacity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        // --- CAMBIO: De TextField a Dropdown ---
        EnumDropdownSelector(
            label = "Status",
            options = statusOptions,
            selectedOption = approved,
            onOptionSelected = onApprovedChange,
            enabled = !isSaving
        )
        // --- FIN DEL CAMBIO ---

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSaveClick,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text("Save Changes", color = Color.White)
            }
        }
    }
}