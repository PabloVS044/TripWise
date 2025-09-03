package uvg.edu.tripwise.discover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.viewModel.PropertyViewModel

class DiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                DiscoverScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(viewModel: PropertyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val properties by viewModel.properties.collectAsState()
    var searchText by remember { mutableStateOf("Madrid") }

    val madrid = LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 6f)
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
                    contentDescription = "Search",
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
                properties.forEach { property ->
                    Marker(
                        state = MarkerState(position = LatLng(property.latitude, property.longitude)),
                        title = property.name,
                        snippet = property.description
                    )
                }
            }
        }
        
        // Properties Found Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "2 propiedades encontradas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Generator Madrid",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                Text(
                    text = "C. de San Bernardo, 2, Centro",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Property Image Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Property Image",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Implement disable action */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Disable", color = Color.Gray)
                    }
                    
                    Button(
                        onClick = { /* TODO: Implement details action */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Details", color = Color.White)
                    }
                }
            }
        }
        
        // Bottom Navigation
        BottomNavigationBar()
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Search, 
                    contentDescription = "Search"
                ) 
            },
            label = { Text("Search") },
            selected = true,
            onClick = { /* Current screen */ },
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
                    imageVector = Icons.Default.Home, 
                    contentDescription = "Home"
                ) 
            },
            label = { Text("Home") },
            selected = false,
            onClick = { /* TODO: Navigate to Home */ },
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
                    contentDescription = "Profile"
                ) 
            },
            label = { Text("Profile") },
            selected = false,
            onClick = { /* TODO: Navigate to Profile */ },
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
