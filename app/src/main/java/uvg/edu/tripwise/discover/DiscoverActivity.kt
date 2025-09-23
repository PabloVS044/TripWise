package uvg.edu.tripwise.discover

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.viewModel.PropertyViewModel
import uvg.edu.tripwise.ui.components.LogoAppTopBar

class DiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                TripWiseTheme {
                    val name = intent.getStringExtra("name") ?: ""
                    val location = intent.getStringExtra("location") ?: ""
                    val minPrice = intent.getStringExtra("minPrice")?.toDoubleOrNull()
                    val maxPrice = intent.getStringExtra("maxPrice")?.toDoubleOrNull()
                    val capacity = intent.getStringExtra("capacity")?.toIntOrNull()
                    val propertyType = intent.getStringExtra("propertyType") ?: ""
                    val approved = intent.getStringExtra("approved") ?: ""
                    DiscoverScreen(
                        filterName = name,
                        filterLocation = location,
                        filterMinPrice = minPrice,
                        filterMaxPrice = maxPrice,
                        filterCapacity = capacity,
                        filterType = propertyType,
                        filterApproved = approved,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: PropertyViewModel = viewModel(),
    filterName: String = "",
    filterLocation: String = "",
    filterMinPrice: Double? = null,
    filterMaxPrice: Double? = null,
    filterCapacity: Int? = null,
    filterType: String = "",
    filterApproved: String = "",
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val searchLabel = stringResource(R.string.search_button)
    val anyPlaceholder = stringResource(R.string.any_placeholder)
    val properties by viewModel.properties.collectAsState()
    val selectedProperty by viewModel.selectedProperty.collectAsState()
    // Search Bar
    var searchText by remember { mutableStateOf("Guatemala") }

    // Filter dropdown state
    var showFilters by remember { mutableStateOf(false) }

    // Filter states
    var name by remember { mutableStateOf(filterName) }
    var minPrice by remember { mutableStateOf(filterMinPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(filterMaxPrice?.toString() ?: "") }
    var capacity by remember { mutableStateOf(filterCapacity?.toString() ?: "") }
    var location by remember { mutableStateOf(filterLocation) }

    // ComboBox Tipo de propiedad
    val propertyTypes = listOf(anyPlaceholder, "Apartamento", "Casa", "Hotel", "Hostel")
    var selectedType by remember { mutableStateOf(if (filterType.isBlank()) propertyTypes.first() else filterType) }
    var typeExpanded by remember { mutableStateOf(false) }

    // ComboBox Aprobación
    val approvalOptions = listOf(anyPlaceholder, "Sí", "No")
    var selectedApproved by remember { mutableStateOf(if (filterApproved.isBlank()) approvalOptions.first() else filterApproved) }
    var approvedExpanded by remember { mutableStateOf(false) }
    val filteredProperties = properties.filter { property ->
        // Filtro por nombre
        val matchesName = name.takeIf { it.isNotBlank() }?.let { searchTerm ->
            property.name.trim().contains(searchTerm.trim(), ignoreCase = true)
        } ?: true

        // Filtro por ubicación
        val matchesLocation = location.takeIf { it.isNotBlank() }?.let { searchTerm ->
            property.location.trim().contains(searchTerm.trim(), ignoreCase = true)
        } ?: true

        // Filtros de precio
        val matchesMinPrice = minPrice.takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { minPriceValue ->
            property.pricePerNight >= minPriceValue
        } ?: true

        val matchesMaxPrice = maxPrice.takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { maxPriceValue ->
            property.pricePerNight <= maxPriceValue
        } ?: true

        // Filtro de capacidad
        val matchesCapacity = capacity.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { requiredCapacity ->
            property.capacity >= requiredCapacity
        } ?: true

        // Filtro por tipo de propiedad
        val matchesType = selectedType.takeIf { it.isNotBlank() && it != anyPlaceholder }?.let { searchType ->
            property.propertyType.trim().contains(searchType.trim(), ignoreCase = true)
        } ?: true

        // Filtro por aprobación
        val matchesApproved = selectedApproved.takeIf { it.isNotBlank() && it != anyPlaceholder }?.let { approvalStatus ->
            when (approvalStatus.lowercase()) {
                "sí", "si", "yes" -> property.approved.contains("sí", ignoreCase = true) ||
                        property.approved.contains("yes", ignoreCase = true) ||
                        property.approved.contains("true", ignoreCase = true)
                "no" -> property.approved.contains("no", ignoreCase = true) ||
                        property.approved.contains("false", ignoreCase = true)
                else -> property.approved.contains(approvalStatus, ignoreCase = true)
            }
        } ?: true

        // Todos los filtros deben coincidir
        matchesName && matchesLocation && matchesMinPrice && matchesMaxPrice &&
                matchesCapacity && matchesType && matchesApproved
    }

    // Coordenadas de Guatemala como ubicación por defecto
    val guatemala = LatLng(14.644734, -90.587886)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(guatemala, 8f)
    }

    Scaffold(
        topBar = {
            LogoAppTopBar(onLogout = onLogout)
        }
    ){
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Status Bar Space
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card de búsqueda (solo diseño)
                Card(
                    modifier = Modifier
                        .weight(1f) // Ocupa todo el espacio disponible
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = searchLabel,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Ícono pequeño de filtros
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filtros",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Filter Card
            if (showFilters) {
                FilterCard(
                    name = name,
                    onNameChange = { name = it },
                    minPrice = minPrice,
                    onMinPriceChange = { minPrice = it },
                    maxPrice = maxPrice,
                    onMaxPriceChange = { maxPrice = it },
                    capacity = capacity,
                    onCapacityChange = { capacity = it },
                    location = location,
                    onLocationChange = { location = it },
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
                    typeExpanded = typeExpanded,
                    onTypeExpandedChange = { typeExpanded = it },
                    propertyTypes = propertyTypes,
                    selectedApproved = selectedApproved,
                    onApprovedChange = { selectedApproved = it },
                    approvedExpanded = approvedExpanded,
                    onApprovedExpandedChange = { approvedExpanded = it },
                    approvalOptions = approvalOptions,
                    onClearFilters = {
                        name = ""
                        minPrice = ""
                        maxPrice = ""
                        capacity = ""
                        location = ""
                        selectedType = propertyTypes.first()
                        selectedApproved = approvalOptions.first()
                    },
                    onClose = { showFilters = false }
                )
            }

            // Google Maps
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapStyleOptions = null
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    filteredProperties.forEach { property ->
                        // Verificar que las coordenadas no sean nulas
                        val latitude = property.latitude ?: 0.0
                        val longitude = property.longitude ?: 0.0

                        if (latitude != 0.0 && longitude != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(latitude, longitude)),
                                title = property.name,
                                snippet = property.description,
                                onClick = {
                                    viewModel.getPropertyById(property.id)
                                    false
                                }
                            )
                        }
                    }
                }
            }

            // Properties Found Section
            selectedProperty?.let { property ->
                PropertyCard(
                    property = property,
                    onClose = { viewModel.clearSelectedProperty() }
                )
            }

            // Bottom Navigation
            BottomNavigationBar(onFilterClick = { showFilters = !showFilters })
    }

    }
}

@Composable
fun PropertyCard(property: Property, onClose: () -> Unit) {
    val closeLabel = stringResource(R.string.close_button)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = property.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeLabel,
                        tint = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = property.description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cargar imagen con Coil
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(property.pictures) { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = property.name,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(280.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_gallery)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterCard(
    name: String,
    onNameChange: (String) -> Unit,
    minPrice: String,
    onMinPriceChange: (String) -> Unit,
    maxPrice: String,
    onMaxPriceChange: (String) -> Unit,
    capacity: String,
    onCapacityChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    selectedType: String,
    onTypeChange: (String) -> Unit,
    typeExpanded: Boolean,
    onTypeExpandedChange: (Boolean) -> Unit,
    propertyTypes: List<String>,
    selectedApproved: String,
    onApprovedChange: (String) -> Unit,
    approvedExpanded: Boolean,
    onApprovedExpandedChange: (Boolean) -> Unit,
    approvalOptions: List<String>,
    onClearFilters: () -> Unit,
    onClose: () -> Unit
) {
    val filterMapLabel = stringResource(R.string.mapFilters)
    val namePlaceholder = stringResource(R.string.name_placeholder)
    val capacityPlaceholder = stringResource(R.string.capacity_placeholder)
    val maxPlaceholder = stringResource(R.string.max_placeholder)
    val minPlaceholder = stringResource(R.string.min_placeholder)
    val locationPlaceholder = stringResource(R.string.location_placeholder)
    val pTypePlaceholder = stringResource(R.string.ptype_placeholder)
    val approvalPlaceholder = stringResource(R.string.approval_placeholder)
    val applyPlaceholder = stringResource(R.string.apply_placeholder)
    val cleanButton = stringResource(R.string.clean_placeholder)
    val closeButton = stringResource(R.string.close)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filterMapLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeButton,
                        tint = Color.Gray
                    )
                }
            }
            
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(namePlaceholder) },
                modifier = Modifier.fillMaxWidth()
            )

            // Precio Min / Max
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = onMinPriceChange,
                    label = { Text(minPlaceholder) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = onMaxPriceChange,
                    label = { Text(maxPlaceholder) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Capacidad
            OutlinedTextField(
                value = capacity,
                onValueChange = onCapacityChange,
                label = { Text(capacityPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text(locationPlaceholder) },
                modifier = Modifier.fillMaxWidth()
            )

            // ComboBox Tipo de propiedad
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = onTypeExpandedChange
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    label = { Text(pTypePlaceholder) },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { onTypeExpandedChange(false) }
                ) {
                    propertyTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                onTypeChange(type)
                                onTypeExpandedChange(false)
                            }
                        )
                    }
                }
            }

            // ComboBox Aprobación
            ExposedDropdownMenuBox(
                expanded = approvedExpanded,
                onExpandedChange = onApprovedExpandedChange
            ) {
                OutlinedTextField(
                    value = selectedApproved,
                    onValueChange = {},
                    label = { Text(approvalPlaceholder) },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(approvedExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = approvedExpanded,
                    onDismissRequest = { onApprovedExpandedChange(false) }
                ) {
                    approvalOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onApprovedChange(option)
                                onApprovedExpandedChange(false)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(cleanButton, color = Color.White)
                }
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(applyPlaceholder)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onFilterClick: () -> Unit = {}) {
    val context = LocalContext.current
    val searchLabel = stringResource(R.string.search_button)
    val profileLabel = stringResource(R.string.profile_button)
    val reservationLabel = stringResource(R.string.reservation_button)
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = searchLabel
                )
            },
            label = { Text(searchLabel) },
            selected = true,
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
            icon = {
                Icon(
                    imageVector = Icons.Default.Luggage,
                    contentDescription = reservationLabel
                )
            },
            label = { Text(reservationLabel) },
            selected = false,
            onClick = {
                val intent = Intent(context, uvg.edu.tripwise.reservation.ReservationPage1Activity::class.java)
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
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = profileLabel
                )
            },
            label = { Text(profileLabel) },
            selected = false,
            onClick = {
                /* navegación al perfil */
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverScreenPreview() {
    TripWiseTheme {
        DiscoverScreen()
    }
}