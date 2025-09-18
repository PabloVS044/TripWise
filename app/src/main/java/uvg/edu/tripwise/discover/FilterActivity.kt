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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
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
    val filterMapLabel = stringResource(R.string.mapFilters)
    val namePlaceholder = stringResource(R.string.name_placeholder)
    val capacityPlaceholder = stringResource(R.string.capacity_placeholder)
    val maxPlaceholder = stringResource(R.string.max_placeholder)
    val minPlaceholder = stringResource(R.string.min_placeholder)
    val locationPlaceholder = stringResource(R.string.location_placeholder)
    val pTypePlaceholder = stringResource(R.string.ptype_placeholder)
    val approvalPlaceholder = stringResource(R.string.approval_placeholder)
    val applyPlaceholder = stringResource(R.string.apply_placeholder)
    val cancelPlaceholder = stringResource(R.string.cancel_placeholder)
    val anyPlaceholder = stringResource(R.string.any_placeholder)

    var name by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // ComboBox Tipo de propiedad
    val propertyTypes = listOf(anyPlaceholder, "Apartamento", "Casa", "Hotel", "Hostel")
    var selectedType by remember { mutableStateOf(propertyTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }

    // ComboBox Aprobación
    val approvalOptions = listOf(anyPlaceholder, "Sí", "No")
    var selectedApproved by remember { mutableStateOf(approvalOptions.first()) }
    var approvedExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            Text(
                text = filterMapLabel,
                modifier = Modifier.weight(1f),
                color = Color.Black,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        }
        // Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(namePlaceholder) },
            modifier = Modifier.fillMaxWidth()
        )

        // Precio Min / Max
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = { minPrice = it },
                label = { Text(minPlaceholder) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                label = { Text(maxPlaceholder) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Capacidad
        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text(capacityPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // ComboBox Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(locationPlaceholder) },
            modifier = Modifier.fillMaxWidth()
        )


        // ComboBox Tipo de propiedad
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
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
                label = { Text(approvalPlaceholder) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(approvedExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
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

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            // Botón Aplicar filtros
            Button(
                onClick = {
                    val intent = Intent(context, DiscoverActivity::class.java).apply {
                        putExtra("name", name.trim())
                        putExtra("minPrice", minPrice.trim())
                        putExtra("maxPrice", maxPrice.trim())
                        putExtra("capacity", capacity.trim())
                        putExtra("location", location.trim())
                        // Solo enviar tipo de propiedad y aprobación si no es "Any"
                        putExtra("propertyType", if (selectedType == anyPlaceholder) "" else selectedType)
                        putExtra("approved", if (selectedApproved == anyPlaceholder) "" else selectedApproved)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text(applyPlaceholder)
            }

            Button(
                onClick = {
                    // valores a su estado inicial
                    name = ""
                    minPrice = ""
                    maxPrice = ""
                    capacity = ""
                    location = ""
                    selectedType = ""
                    selectedApproved = ""

                    // Intent para regresar sin filtros
                    val intent = Intent(context, DiscoverActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(cancelPlaceholder)
            }

        }
    }
}



