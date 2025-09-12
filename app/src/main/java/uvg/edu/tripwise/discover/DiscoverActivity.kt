package uvg.edu.tripwise.discover

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.w3c.dom.Text
import uvg.edu.tripwise.BottomNavigation
import uvg.edu.tripwise.data.model.Post
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.viewModel.PropertyViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.request.ImageRequest
import uvg.edu.tripwise.R


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
                    filterApproved = approved
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: PropertyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    filterName: String = "",
    filterLocation: String = "",
    filterMinPrice: Double? = null,
    filterMaxPrice: Double? = null,
    filterCapacity: Int? = null,
    filterType: String = "",
    filterApproved: String = ""
) {

    val searchLabel = stringResource(R.string.search_button)
    val properties by viewModel.properties.collectAsState()
    val selectedProperty by viewModel.selectedProperty.collectAsState()
    // Search Bar
    var searchText by remember { mutableStateOf("Guatemala") }
    val filteredProperties = properties.filter { property ->
        // Filtro por nombre 
        val matchesName = filterName.takeIf { it.isNotBlank() }?.let { searchTerm ->
            property.name.trim().contains(searchTerm.trim(), ignoreCase = true)
        } ?: true

        // Filtro por ubicación 
        val matchesLocation = filterLocation.takeIf { it.isNotBlank() }?.let { searchTerm ->
            property.location.trim().contains(searchTerm.trim(), ignoreCase = true)
        } ?: true

        // Filtros de precio
        val matchesMinPrice = filterMinPrice?.let { minPrice -> 
            property.pricePerNight >= minPrice 
        } ?: true
        
        val matchesMaxPrice = filterMaxPrice?.let { maxPrice -> 
            property.pricePerNight <= maxPrice 
        } ?: true
        
        // Filtro de capacidad 
        val matchesCapacity = filterCapacity?.let { requiredCapacity -> 
            property.capacity >= requiredCapacity 
        } ?: true

        // Filtro por tipo de propiedad
        val matchesType = filterType.takeIf { it.isNotBlank() && it != "Apartamento" }?.let { searchType ->
            property.propertyType.trim().contains(searchType.trim(), ignoreCase = true)
        } ?: true

        // Filtro por aprobación
        val matchesApproved = filterApproved.takeIf { it.isNotBlank() && it != "Sí" }?.let { approvalStatus ->
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Status Bar Space
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = searchText,
                    modifier = Modifier.weight(1f),
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = searchLabel,
                    tint = Color.Gray
                )
            }
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
                    Marker(
                        state = MarkerState(position = LatLng(property.latitude, property.longitude)),
                        title = property.name,
                        snippet = property.description,
                        onClick = {
                            viewModel.getPropertyById(property._id)
                            false
                        }
                    )
                }
            }
        }
        
        // Properties Found Section
        selectedProperty?.let { property ->
            PropertyCard(
                property = property,
                onClose = {viewModel.clearSelectedProperty()}
                )
        }

        // Bottom Navigation
        BottomNavigationBar()
    }
}
@Composable
fun PropertyCard(property: Post, onClose: () -> Unit) {
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

            //cargar imagen con Coil o Glide
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


@Composable
fun BottomNavigationBar() {
    val searchLabel = stringResource(R.string.search_button)
    val reservationLabel = stringResource(R.string.reservation_button)
    val filterLabel = stringResource(R.string.filter_button)
    val profileLabel = stringResource(R.string.profile_button)

    val context = LocalContext.current
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
            onClick = { val intent = Intent(context, DiscoverActivity::class.java)
                context.startActivity(intent) },
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
            onClick = { val intent = Intent(context, uvg.edu.tripwise.reservation.ReservationPage1Activity::class.java)
                context.startActivity(intent)},
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
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = filterLabel
                )
            },
            label = { Text(filterLabel) },
            selected = false,
            onClick = { val intent = Intent(context, FilterActivity::class.java)
                context.startActivity(intent) },
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
                /*navegación al perfil */
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
