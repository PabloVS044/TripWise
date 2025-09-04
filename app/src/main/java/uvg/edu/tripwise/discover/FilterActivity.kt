package uvg.edu.tripwise.discover

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import java.util.logging.Filter

class FilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                FilterScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen() {
    var name by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    // ComboBox Location
    val locations = listOf("España", "Guatemala", "Francia", "México")
    var selectedLocation by remember { mutableStateOf(locations.first()) }
    var locationExpanded by remember { mutableStateOf(false) }

    // ComboBox Tipo de propiedad
    val propertyTypes = listOf("Apartamento", "Casa", "Hotel", "Hostel")
    var selectedType by remember { mutableStateOf(propertyTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }

    // ComboBox Aprobación
    val approvalOptions = listOf("Sí", "No")
    var selectedApproved by remember { mutableStateOf(approvalOptions.first()) }
    var approvedExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        // Precio Min / Max
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = { minPrice = it },
                label = { Text("Precio Min") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                label = { Text("Precio Max") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Capacidad
        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text("Capacidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // ComboBox Location
        ExposedDropdownMenuBox(
            expanded = locationExpanded,
            onExpandedChange = { locationExpanded = !locationExpanded }
        ) {
            OutlinedTextField(
                value = selectedLocation,
                onValueChange = {},
                label = { Text("Location") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                locations.forEach { loc ->
                    DropdownMenuItem(
                        text = { Text(loc) },
                        onClick = {
                            selectedLocation = loc
                            locationExpanded = false
                        }
                    )
                }
            }
        }

        // ComboBox Tipo de propiedad
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                label = { Text("Tipo de propiedad") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                propertyTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // ComboBox Aprobación
        ExposedDropdownMenuBox(
            expanded = approvedExpanded,
            onExpandedChange = { approvedExpanded = !approvedExpanded }
        ) {
            OutlinedTextField(
                value = selectedApproved,
                onValueChange = {},
                label = { Text("Aprobación") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(approvedExpanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = approvedExpanded,
                onDismissRequest = { approvedExpanded = false }
            ) {
                approvalOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedApproved = option
                            approvedExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Aplicar filtros
        Button(
            onClick = {
                val intent = Intent(context, DiscoverActivity::class.java).apply {
                    putExtra("name", name)
                    putExtra("minPrice", minPrice)
                    putExtra("maxPrice", maxPrice)
                    putExtra("capacity", capacity)
                    putExtra("location", selectedLocation)
                    putExtra("propertyType", selectedType)
                    putExtra("approved", selectedApproved)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar filtros")
        }
    }
}



