package uvg.edu.tripwise.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.components.BottomNavigation
import uvg.edu.tripwise.components.PropertyCard
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    onLogout: () -> Unit = {}
) {
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var filteredProperties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val propertyRepository = remember { PropertyRepository() }

    Scaffold (topBar = { LogoAppTopBar(onLogout) }) {
            innerPadding ->
        fun loadProperties() {
            scope.launch {
                try {
                    isLoading = true
                    isError = false
                    Log.d("PropertiesActivity", "Loading properties...")

                    properties = propertyRepository.getProperties()
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
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {

            Spacer(modifier = Modifier.height(24.dp))


            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.search_properties))
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFE2E8F0),
                    unfocusedContainerColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { refreshProperties() },
                modifier = Modifier.weight(1f)
            ) {
                when {
                    isLoading && !isRefreshing -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF2563EB))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.loading_properties), color = Color.Gray)
                            }
                        }
                    }
                    isError -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.error_loading_properties),
                                    color = Color.Red,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { loadProperties() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text(stringResource(R.string.retry), color = Color.White)
                                }
                            }
                        }
                    }
                    filteredProperties.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) stringResource(R.string.no_properties_found) else stringResource(R.string.no_match_search),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredProperties) { property ->
                                PropertyCard(
                                    property = property,
                                    onRefresh = { loadProperties() }
                                )
                            }
                        }
                    }
                }
            }

            BottomNavigation(context = context, currentScreen = stringResource(R.string.properties_title))
        }
    }

}