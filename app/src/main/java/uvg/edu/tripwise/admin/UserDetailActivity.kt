package uvg.edu.tripwise.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class UserDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("userId") ?: ""
        setContent {
            TripWiseTheme {
                UserDetailScreen(userId = userId, onBack = { finish() }, onLogout = {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(userId: String, onBack: () -> Unit, onLogout: () -> Unit = {}) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val users = userRepository.getUsers()
                user = users.find { it.id == userId }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            LogoAppTopBar(onLogout)
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("User not found", color = Color.Gray)
            }
        } else {
            UserDetailContent(user = user!!, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun UserDetailContent(user: User, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Name", value = user.name)
                InfoRow(label = "Email", value = user.email)
                InfoRow(label = "Status", value = if (user.deleted?.isDeleted == true) "Inactive" else "Active")
                InfoRow(label = "User ID", value = user.id)
            }
        }

        Text(
            text = "Edit User",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Button(
            onClick = { /* TODO: Save changes */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Save Changes", color = Color.White)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}