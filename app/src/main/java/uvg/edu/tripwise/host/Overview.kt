package uvg.edu.tripwise.host

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository

/* Colores locales (mismos valores visuales que en PropertiesHost.kt) */
private val PrimaryBlue = Color(0xFF2563EB)
private val SuccessGreen = Color(0xFF0AA12E)

/** == Carga remota de la propiedad (igual contrato que antes) == */
@Composable
fun SummarySectionRemote(
    propertyId: String?,
    repo: PropertyRepository = PropertyRepository(),
    reloadKey: Int = 0
) {
    var property by remember { mutableStateOf<Property?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(propertyId, reloadKey) {
        val id = propertyId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        try {
            loading = true
            property = repo.getPropertyById(id)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    when {
        propertyId.isNullOrBlank() -> PlaceholderSection(stringResource(R.string.select_property_to_see_overview))
        loading -> PlaceholderSection(stringResource(R.string.loading_property))
        error != null -> PlaceholderSection(stringResource(R.string.error_with_message, error ?: ""))
        property == null -> PlaceholderSection(stringResource(R.string.property_not_found))
        else -> SummarySectionBound(property = property!!)
    }
}

/** == UI del Overview (usa helpers expuestos desde PropertiesHost.kt) == */
@Composable
fun SummarySectionBound(property: Property) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhiteCard(title = stringResource(R.string.basic_information)) {
            KeyValueRow(stringResource(R.string.label_type), property.propertyType.ifBlank { stringResource(R.string.placeholder_em_dash) })
            KeyValueRow(stringResource(R.string.label_guests), property.capacity.toString())
            KeyValueRow(stringResource(R.string.label_rooms), stringResource(R.string.placeholder_em_dash))
            KeyValueRow(stringResource(R.string.label_bathrooms), stringResource(R.string.placeholder_em_dash))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_price_per_night), fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
                Text("$${property.pricePerNight}", fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
        }

        WhiteCard(title = stringResource(R.string.amenities_title)) {
            val amenities = property.amenities
            if (amenities.isEmpty()) {
                Text(stringResource(R.string.placeholder_em_dash), color = Color(0xFF26364D))
            } else {
                AmenitiesGrid(amenities)
            }
        }

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.description_title), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(property.description.ifBlank { stringResource(R.string.placeholder_em_dash) }, color = Color(0xFF102A43), lineHeight = 20.sp)
            }
        }
    }
}
