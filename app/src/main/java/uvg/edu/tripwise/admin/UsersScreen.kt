package uvg.edu.tripwise.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import uvg.edu.tripwise.components.BottomNavigation
import uvg.edu.tripwise.components.UserCard
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen( onLogout: () -> Unit = {} ) {
    var searchQuery by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<User>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRetry by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    Scaffold(topBar = { LogoAppTopBar(onLogout) }) {
        innerPadding ->
        fun loadUsers() {
            coroutineScope.launch {
                try {
                    isLoading = true
                    errorMessage = null
                    showRetry = false

                    Log.d("UsersActivity", "Loading users from API...")
                    users = userRepository.getUsers()
                    Log.d("UsersActivity", "Successfully mapped ${users.size} users")
                } catch (e: Exception) {
                    Log.e("UsersActivity", "Error loading users: ${e.message}", e)
                    errorMessage = "Error al cargar usuarios: ${e.message}"
                    showRetry = true
                } finally {
                    isLoading = false
                    isRefreshing = false
                }
            }
        }

        fun refreshUsers() {
            isRefreshing = true
            loadUsers()
        }

        LaunchedEffect(Unit) {
            loadUsers()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            Spacer(modifier = Modifier.height(24.dp))


            // Search Bar (mismo estilo que Users)
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
                        Text("Search users...")
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
                            Text("Cargando usuarios...", color = Color.Gray)
                        }
                    }
                }
                showRetry -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Error desconocido",
                                color = Color.Red,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { loadUsers() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                }
                else -> {
                    val filteredUsers = users.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.email.contains(searchQuery, ignoreCase = true)
                    }

                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing),
                        onRefresh = { refreshUsers() },
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredUsers) { user ->
                                UserCard(user = user, onRefresh = { loadUsers() })
                            }
                        }
                    }
                }
            }

            BottomNavigation(context = context)
    }

    }
}