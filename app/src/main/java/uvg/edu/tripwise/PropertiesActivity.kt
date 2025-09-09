package uvg.edu.tripwise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.Property
import uvg.edu.tripwise.network.RetrofitInstance
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

class PropertiesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PropertiesScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen() {
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var filteredProperties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadProperties() {
        scope.launch {
            try {
                isLoading = true
                isError = false
                Log.d("PropertiesActivity", "Loading properties...")

                val apiProperties = RetrofitInstance.api.getProperties()
                Log.d("PropertiesActivity", "API Response: $apiProperties")

                properties = apiProperties.map { apiProperty ->
                    Property(
                        id = apiProperty._id,
                        name = apiProperty.name,
                        description = apiProperty.description,
                        location = apiProperty.location,
                        pricePerNight = apiProperty.pricePerNight,
                        capacity = apiProperty.capacity,
                        pictures = apiProperty.pictures,
                        amenities = apiProperty.amenities,
                        propertyType = apiProperty.propertyType,
                        owner = apiProperty.owner,
                        approved = apiProperty.approved,
                        latitude = apiProperty.latitude,
                        longitude = apiProperty.longitude,
                        createdAt = apiProperty.createdAt,
                        isDeleted = apiProperty.deleted.`is`
                    )
                }
                filteredProperties = properties
                Log.d("PropertiesActivity", "Properties loaded: ${properties.size}")
            } catch (e: Exception) {
                Log.e("PropertiesActivity", "Error loading properties", e)
                isError = true
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun refreshProperties() {
        isRefreshing = true
        loadProperties()
    }

    LaunchedEffect(Unit) {
        loadProperties()
    }

    LaunchedEffect(searchQuery) {
        filteredProperties = if (searchQuery.isEmpty()) {
            properties
        } else {
            properties.filter { property ->
                property.name.contains(searchQuery, ignoreCase = true) ||
                        property.location.contains(searchQuery, ignoreCase = true) ||
                        property.propertyType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Status Bar
        Spacer(modifier = Modifier.height(24.dp))

        // Header with time and status icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "9:30",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Black, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(10.dp)
                        .background(Color.Black, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(6.dp)
                        .background(Color.Black, RoundedCornerShape(1.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Search properties...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8F0),
                unfocusedContainerColor = Color(0xFFE8E8F0),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { refreshProperties() },
            modifier = Modifier.weight(1f)
        ) {
            when {
                isLoading && !isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }
                isError -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading properties",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadProperties() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
                filteredProperties.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No properties found" else "No properties match your search",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProperties) { property ->
                            PropertyCard(
                                property = property,
                                onDisableClick = {
                                    scope.launch {
                                        try {
                                            RetrofitInstance.api.deleteProperty(property.id)
                                            loadProperties()
                                        } catch (e: Exception) {
                                            Log.e("PropertiesActivity", "Error disabling property", e)
                                        }
                                    }
                                },
                                onDetailsClick = {
                                    // TODO: Navigate to property details
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavigation(context = context, currentScreen = "Properties")
    }
}

@Composable
fun PropertyCard(
    property: Property,
    onDisableClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = property.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = property.location,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (property.approved) {
                        "approved" -> Color(0xFF2196F3)
                        "pending" -> Color(0xFFFF9800)
                        "rejected" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = property.approved.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Property Image
            if (property.pictures.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(property.pictures.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = property.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.ic_menu_gallery)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDisableClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Disable")
                }

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Details", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(context: android.content.Context, currentScreen: String = "") {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2563EB),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CustomBottomNavItem(
                title = "Dashboard",
                icon = Icons.Default.Dashboard,
                isSelected = currentScreen == "Dashboard",
                onClick = { /* TODO: Navigate to Dashboard */ }
            )
            CustomBottomNavItem(
                title = "Users",
                icon = Icons.Default.People,
                isSelected = currentScreen == "Users",
                onClick = {
                    context.startActivity(Intent(context, UsersActivity::class.java))
                }
            )
            CustomBottomNavItem(
                title = "Properties",
                icon = Icons.Default.Home,
                isSelected = currentScreen == "Properties",
                onClick = { /* Current screen, no action needed */ }
            )
        }
    }
}

@Composable
fun CustomBottomNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}