package uvg.edu.tripwise.auth.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.components.AppLogoHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertySetupScreen(
    propertyName: String,
    propertyDescription: String,
    propertyLocation: String,
    pricePerNight: String,
    capacity: String,
    propertyType: String,
    selectedAmenities: Set<String>,
    onPropertyNameChange: (String) -> Unit,
    onPropertyDescriptionChange: (String) -> Unit,
    onPropertyLocationChange: (String) -> Unit,
    onPricePerNightChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onSelectedAmenitiesChange: (Set<String>) -> Unit,
    onLatitudeChange: (Double?) -> Unit,
    onLongitudeChange: (Double?) -> Unit,
    totalSteps: Int = 4, // Added parameter, always 4 for owner
    modifier: Modifier = Modifier
) {
    val propertyTypes = listOf(
        PropertyTypeItem(stringResource(R.string.house), Icons.Default.House, "casa"),
        PropertyTypeItem(stringResource(R.string.apartment), Icons.Default.Apartment, "apartamento"),
        PropertyTypeItem(stringResource(R.string.cabin), Icons.Default.Cabin, "cabana"),
        PropertyTypeItem(stringResource(R.string.hotel), Icons.Default.Hotel, "hotel")
    )

    val availableAmenities = listOf(
        AmenityItem(stringResource(R.string.wifi), Icons.Default.Wifi, "wifi"),
        AmenityItem(stringResource(R.string.pool), Icons.Default.Pool, "piscina"),
        AmenityItem(stringResource(R.string.kitchen), Icons.Default.Kitchen, "cocina"),
        AmenityItem(stringResource(R.string.parking), Icons.Default.LocalParking, "estacionamiento"),
        AmenityItem(stringResource(R.string.air_conditioning), Icons.Default.AcUnit, "aire_acondicionado"),
        AmenityItem(stringResource(R.string.tv), Icons.Default.Tv, "tv"),
        AmenityItem(stringResource(R.string.washing_machine), Icons.Default.LocalLaundryService, "lavadora"),
        AmenityItem(stringResource(R.string.balcony), Icons.Default.Balcony, "balcon")
    )
    val guatemala = LatLng(14.6349, -90.5069)
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) } 
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) }
        item {
            AppLogoHeader(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }
        item {
            Text(
                text = stringResource(R.string.describe_property_message),
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                StepIndicator(
                    currentStep = 3,
                    totalSteps = totalSteps
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.your_property),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            OutlinedTextField(
                value = propertyName,
                onValueChange = onPropertyNameChange,
                label = { Text(stringResource(R.string.property_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    focusedLabelColor = Color(0xFF2563EB)
                )
            )
        }
        item {
            OutlinedTextField(
                value = propertyDescription,
                onValueChange = onPropertyDescriptionChange,
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    focusedLabelColor = Color(0xFF2563EB)
                ),
                maxLines = 4
            )
        }
        item {
            OutlinedTextField(
                value = propertyLocation,
                onValueChange = onPropertyLocationChange,
                label = { Text(stringResource(R.string.location)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    focusedLabelColor = Color(0xFF2563EB)
                )
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pricePerNight,
                    onValueChange = onPricePerNightChange,
                    label = { Text(stringResource(R.string.price_per_night)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    )
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = onCapacityChange,
                    label = { Text(stringResource(R.string.capacity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    )
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.property_type),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(propertyTypes) { type ->
                    PropertyTypeCard(
                        propertyType = type,
                        isSelected = propertyType == type.key,
                        onClick = { onPropertyTypeChange(type.key) }
                    )
                }
            }
        }
        item{
            Text(
                text = stringResource(R.string.location),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        item{
            LocationPickerMap { latLng ->
                onLatitudeChange(latLng.latitude)
                onLongitudeChange(latLng.longitude)
            }
        }
        item {
            Text(
                text = stringResource(R.string.amenities),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableAmenities.chunked(2).forEach { rowAmenities ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowAmenities.forEach { amenity ->
                            AmenityChip(
                                amenity = amenity,
                                isSelected = selectedAmenities.contains(amenity.key),
                                onClick = {
                                    val newAmenities = if (selectedAmenities.contains(amenity.key)) {
                                        selectedAmenities - amenity.key
                                    } else {
                                        selectedAmenities + amenity.key
                                    }
                                    onSelectedAmenitiesChange(newAmenities)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowAmenities.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

data class PropertyTypeItem(
    val name: String,
    val icon: ImageVector,
    val key: String
)

data class AmenityItem(
    val name: String,
    val icon: ImageVector,
    val key: String
)

@Composable
private fun PropertyTypeCard(
    propertyType: PropertyTypeItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(100.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FF) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = propertyType.icon,
                    contentDescription = propertyType.name,
                    tint = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = propertyType.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF2563EB) else Color.Black
            )
        }
    }
}

@Composable
private fun AmenityChip(
    amenity: AmenityItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FF) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = amenity.icon,
                contentDescription = amenity.name,
                tint = if (isSelected) Color(0xFF2563EB) else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amenity.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF2563EB) else Color.Black
            )
        }
    }
}

@Composable
fun LocationPickerMap(
    modifier: Modifier = Modifier,
    defaultPosition: LatLng = LatLng(14.6349, -90.5069), // Guatemala
    onLocationSelected: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 8f)
    }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.select_location),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    onLocationSelected(latLng)
                },
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = stringResource(R.string.selected_location),
                        snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    )
                }
            }
        }
    }
}