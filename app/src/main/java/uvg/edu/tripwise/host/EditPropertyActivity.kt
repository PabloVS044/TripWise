package uvg.edu.tripwise.host

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import uvg.edu.tripwise.R
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UpdatePropertyRequest
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class EditPropertyActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PROPERTY_ID = "extra_property_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID)
        if (propertyId == null) {
            finish()
            return
        }

        setContent {
            TripWiseTheme {
                EditPropertyScreen(
                    propertyId = propertyId,
                    onBack = { finish() },
                    onSaved = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

/* ========================= TOKENS / PALETA ========================= */
private val BrandBlue    = Color(0xFF1F47B2)
private val SelectedBlue = Color(0xFF2F5BFF)
private val PageBg       = Color(0xFFF7F7FB)
private val SoftGray     = Color(0xFFE8ECF2)
private val SelectedBg   = Color(0xFFEFF4FF)
private val Corner       = 12.dp

// TextFields
private const val FIELD_WIDTH_FRACTION = 1f
private val FIELD_MIN_HEIGHT           = 52.dp

// Property Type
private val TYPE_CARD_WIDTH         = 110.dp
private val TYPE_CARD_HEIGHT        = 96.dp
private val TYPE_ICON_BOX_WIDTH     = 36.dp
private val TYPE_ICON_BOX_HEIGHT    = 32.dp
private val TYPE_TEXT_SIZE          = 12.sp

// Amenities
private val AMENITY_CARD_HEIGHT     = 50.dp
private val AMENITY_ICON_BOX_WIDTH  = 30.dp
private val AMENITY_ICON_BOX_HEIGHT = 22.dp
private val AMENITY_TEXT_SIZE       = 14.sp

/* ========================= UI PRINCIPAL ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyScreen(
    propertyId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---------- STATE ----------
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    data class PType(val key: String, val label: String, val icon: ImageVector)
    val propertyTypes = listOf(
        PType("house", stringResource(R.string.type_house), Icons.Filled.Home),
        PType("apartment", stringResource(R.string.type_apartment), Icons.Filled.Apartment),
        PType("cottage", stringResource(R.string.type_cabin), Icons.Filled.HolidayVillage),
        PType("hotel", stringResource(R.string.type_hotel), Icons.Filled.Hotel)
    )
    var selectedType by remember { mutableStateOf<PType?>(null) }

    data class Amenity(val name: String, val icon: ImageVector)
    val amenityPresets = listOf(
        Amenity(stringResource(R.string.amenity_wifi), Icons.Filled.Wifi),
        Amenity(stringResource(R.string.amenity_pool), Icons.Filled.Pool),
        Amenity(stringResource(R.string.amenity_kitchen), Icons.Filled.Restaurant),
        Amenity(stringResource(R.string.amenity_parking), Icons.Filled.LocalParking),
        Amenity(stringResource(R.string.amenity_air_conditioning), Icons.Filled.AcUnit),
        Amenity(stringResource(R.string.amenity_tv), Icons.Filled.Tv),
        Amenity(stringResource(R.string.amenity_washing_machine), Icons.Filled.LocalLaundryService),
        Amenity(stringResource(R.string.amenity_balcony), Icons.Filled.Balcony)
    )
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }

    var latitude  by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    val defaultLatLng = LatLng(14.634915, -90.506882)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 8f)
    }

    var pictureUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoadingInitial by remember { mutableStateOf(true) }

    // ---------- CARGA INICIAL DE LA PROPIEDAD ----------
    LaunchedEffect(propertyId) {
        try {
            isLoadingInitial = true
            val apiProperty = RetrofitInstance.api.getPropertyById(propertyId)

            name        = apiProperty.name
            description = apiProperty.description
            location    = apiProperty.location
            price       = apiProperty.pricePerNight.toString()
            capacity    = apiProperty.capacity.toString()
            latitude    = apiProperty.latitude
            longitude   = apiProperty.longitude
            pictureUrls = apiProperty.pictures
            selectedAmenities = apiProperty.amenities.toSet()
            selectedType = propertyTypes.firstOrNull { it.key == apiProperty.propertyType }

            apiProperty.latitude?.let { lat ->
                apiProperty.longitude?.let { lng ->
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(lat, lng), 12f)
                }
            }
        } catch (e: Exception) {
            Log.e("EditProperty", "Error cargando propiedad", e)
            errorMsg = e.message ?: context.getString(R.string.network_error)
        } finally {
            isLoadingInitial = false
        }
    }

    // ---------- PICKER DE IMÁGENES ----------
    val imagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult

        uploading = true
        scope.launch {
            try {
                val resolver = context.contentResolver
                val newUrls = mutableListOf<String>()

                for (u in uris) {
                    val mimeType = resolver.getType(u) ?: "image/*"
                    val inputStream = resolver.openInputStream(u) ?: continue
                    val bytes = inputStream.use { it.readBytes() }
                    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val fileName = "prop_${System.currentTimeMillis()}.jpg"

                    val part = MultipartBody.Part.createFormData(
                        "imagen",
                        fileName,
                        requestBody
                    )

                    val uploadResponse = RetrofitInstance.api.uploadImage(part)
                    newUrls += uploadResponse.url
                }

                pictureUrls = (pictureUrls + newUrls).distinct()
            } catch (e: Exception) {
                Log.e("EditProperty", "Error subiendo imagen", e)
                errorMsg = e.message ?: context.getString(R.string.error_uploading_image)
            } finally {
                uploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                LogoAppTopBar(onLogout = {})
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = BrandBlue
                    )
                }
            }
        }
    ) { inner ->

        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(PageBg)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.edit_property),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
            Spacer(Modifier.height(12.dp))

            if (isLoadingInitial) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 32.dp),
                    color = SelectedBlue
                )
                return@Column
            }

            // ====== Inputs ======
            StyledTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.label_property_name),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.label_description),
                minHeight = 120.dp
            )
            Spacer(Modifier.height(12.dp))
            StyledTextField(
                value = location,
                onValueChange = { location = it },
                label = stringResource(R.string.label_location),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StyledTextField(
                    value = price,
                    onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = stringResource(R.string.label_price_night_usd),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                StyledTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter { ch -> ch.isDigit() } },
                    label = stringResource(R.string.label_capacity),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.property_type_title), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(propertyTypes.size) { idx ->
                    val type = propertyTypes[idx]
                    val selected = selectedType?.key == type.key
                    TypeCard(
                        label = type.label,
                        icon = type.icon,
                        selected = selected,
                        onClick = { selectedType = type },
                        width = TYPE_CARD_WIDTH,
                        height = TYPE_CARD_HEIGHT
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.location_section_title), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(Corner),
                border = BorderStroke(1.dp, SoftGray),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                var marker by remember { mutableStateOf<LatLng?>(null) }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true),
                    onMapClick = { latLng ->
                        marker = latLng
                        latitude = latLng.latitude
                        longitude = latLng.longitude
                    }
                ) {
                    val lat = latitude
                    val lng = longitude
                    when {
                        marker != null -> {
                            Marker(
                                state = MarkerState(position = marker!!),
                                title = stringResource(R.string.map_marker_selected)
                            )
                        }
                        lat != null && lng != null -> {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = stringResource(R.string.map_marker_selected)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StyledTextField(
                    value = latitude?.toString().orEmpty(),
                    onValueChange = { latitude = it.toDoubleOrNull() },
                    label = stringResource(R.string.label_latitude),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                StyledTextField(
                    value = longitude?.toString().orEmpty(),
                    onValueChange = { longitude = it.toDoubleOrNull() },
                    label = stringResource(R.string.label_longitude),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.amenities_title), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                amenityPresets.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { amenity ->
                            val sel = amenity.name in selectedAmenities
                            AmenityCard(
                                label = amenity.name,
                                icon = amenity.icon,
                                selected = sel,
                                onClick = {
                                    selectedAmenities = if (sel) {
                                        selectedAmenities - amenity.name
                                    } else {
                                        selectedAmenities + amenity.name
                                    }
                                },
                                height = AMENITY_CARD_HEIGHT,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.pictures_title), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SelectedBg,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isSubmitting && !uploading) {
                            imagesPicker.launch("image/*")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = stringResource(R.string.cd_add_images),
                            tint = SelectedBlue
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                val picsText = when {
                    uploading -> stringResource(R.string.uploading_images)
                    pictureUrls.isEmpty() -> stringResource(R.string.no_images_selected)
                    else -> stringResource(R.string.images_selected_count, pictureUrls.size)
                }
                Text(text = picsText, color = Color(0xFF475569))
            }

            if (pictureUrls.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    itemsIndexed(pictureUrls) { index, url ->
                        Box(
                            modifier = Modifier.size(80.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SelectedBg,
                                modifier = Modifier.matchParentSize()
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = stringResource(R.string.cd_property_image),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            IconButton(
                                onClick = {
                                    pictureUrls = pictureUrls.toMutableList().also {
                                        if (index in it.indices) it.removeAt(index)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .size(24.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_remove_image),
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            errorMsg?.let { msg ->
                Text(
                    msg,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(Corner),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SelectedBlue),
                    border = BorderStroke(1.5.dp, SelectedBlue)
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }

                Button(
                    onClick = {
                        val priceVal = price.toDoubleOrNull()
                        val capVal = capacity.toIntOrNull()
                        when {
                            name.isBlank() || description.isBlank() || location.isBlank() ->
                                errorMsg = context.getString(R.string.error_fill_name_desc_location)
                            priceVal == null || capVal == null ->
                                errorMsg = context.getString(R.string.error_price_capacity_numeric)
                            selectedType == null ->
                                errorMsg = context.getString(R.string.error_select_property_type)
                            latitude == null || longitude == null ->
                                errorMsg = context.getString(R.string.error_valid_coordinates)
                            else -> {
                                errorMsg = null
                                scope.launch {
                                    isSubmitting = true
                                    try {
                                        val req = UpdatePropertyRequest(
                                            name = name,
                                            description = description,
                                            location = location,
                                            pricePerNight = priceVal,
                                            capacity = capVal,
                                            pictures = pictureUrls,
                                            amenities = selectedAmenities.toList(),
                                            propertyType = selectedType!!.key,
                                            latitude = latitude,
                                            longitude = longitude
                                        )

                                        val resp = RetrofitInstance.api.updateProperty(
                                            propertyId,
                                            req
                                        )

                                        if (resp.isSuccessful) {
                                            onSaved()
                                        } else {
                                            val body = resp.errorBody()?.string()
                                            errorMsg = body ?: context.getString(
                                                R.string.server_error_with_code,
                                                resp.code()
                                            )
                                            Log.e(
                                                "EditProperty",
                                                "updateProperty failed: code=${resp.code()} body=$body"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        errorMsg = e.message
                                            ?: context.getString(R.string.network_error)
                                        Log.e(
                                            "EditProperty",
                                            "exception on updateProperty",
                                            e
                                        )
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting && !uploading,
                    shape = RoundedCornerShape(Corner),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SelectedBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(R.string.action_confirm_settings))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ========================= SUBCOMPONENTES REUTILIZADOS ========================= */
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    minHeight: Dp = FIELD_MIN_HEIGHT,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth(FIELD_WIDTH_FRACTION)
            .heightIn(min = minHeight)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(Corner),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SelectedBlue,
            unfocusedBorderColor = SoftGray,
            focusedContainerColor = SelectedBg,
            unfocusedContainerColor = Color.White,
            cursorColor = Color.Black
        )
    )
}

@Composable
private fun TypeCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    val borderColor = if (selected) SelectedBlue else SoftGray
    val bgColor = if (selected) SelectedBg else Color.White
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(Corner),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = bgColor),
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selected) SelectedBlue else Color(0xFFF2F4F8)
            ) {
                Box(
                    Modifier.size(
                        width = TYPE_ICON_BOX_WIDTH,
                        height = TYPE_ICON_BOX_HEIGHT
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else Color(0xFF6B7280)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                fontSize = TYPE_TEXT_SIZE,
                color = if (selected) SelectedBlue else Color.Black
            )
        }
    }
}

@Composable
private fun AmenityCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) SelectedBlue else SoftGray
    val bgColor = if (selected) SelectedBg else Color.White
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(Corner),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = bgColor),
        modifier = modifier.height(height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Box(
                Modifier.size(
                    width = AMENITY_ICON_BOX_WIDTH,
                    height = AMENITY_ICON_BOX_HEIGHT
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) SelectedBlue else Color(0xFF6B7280)
                )
            }
            Text(
                label,
                fontSize = AMENITY_TEXT_SIZE,
                color = if (selected) SelectedBlue else Color.Black
            )
        }
    }
}
