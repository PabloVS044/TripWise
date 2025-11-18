package uvg.edu.tripwise.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class UserDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("userId") ?: ""
        setContent {
            TripWiseTheme {
                UserDetailScreen(userId = userId, onBack = { finish() }, onLogout = {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    // Estados para los campos editables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                isLoading = true
                val u = userRepository.getUserById(userId)
                user = u

                name = u.name
                email = u.email
                role = u.role ?: "user"
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_loading_user), Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun onSaveChanges() {
        val currentUser = user ?: return

        scope.launch {
            isSaving = true
            try {
                userRepository.updateUser(
                    id = currentUser.id,
                    name = name,
                    email = email,
                    pfp = currentUser.pfp,
                    role = role,
                    interests = currentUser.interests
                )
                Toast.makeText(context, context.getString(R.string.user_updated_success), Toast.LENGTH_SHORT).show()
                onBack()

            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_saving_changes_detail, e.message), Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.user_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.cd_logout))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (user == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.user_not_found), color = Color.Gray)
            }
        } else {
            UserDetailContent(
                modifier = Modifier.padding(innerPadding),
                user = user!!,
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                role = role,
                onRoleChange = { role = it },
                isSaving = isSaving,
                onSaveClick = { onSaveChanges() }
            )
        }
    }
}

@Composable
fun UserDetailContent(
    modifier: Modifier = Modifier,
    user: User,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    val roleOptions = listOf("user", "owner", "admin")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = stringResource(R.string.user_id), value = user.id)
                InfoRow(
                    label = stringResource(R.string.status),
                    value = if (user.deleted?.isDeleted == true)
                        stringResource(R.string.status_inactive)
                    else
                        stringResource(R.string.status_active)
                )
            }
        }

        Text(
            text = stringResource(R.string.edit_user),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        EnumDropdownSelector(
            label = stringResource(R.string.role),
            options = roleOptions,
            selectedOption = role,
            onOptionSelected = onRoleChange,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSaveClick,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(stringResource(R.string.save_changes), color = Color.White)
            }
        }
    }
}