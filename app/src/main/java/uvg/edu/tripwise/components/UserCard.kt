package uvg.edu.tripwise.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.admin.UserDetailActivity
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository

@Composable
fun UserCard(user: User, onRefresh: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current
    val initial = user.name.firstOrNull()?.uppercase() ?: stringResource(R.string.default_avatar_initial)
    val isActive = user.deleted?.isDeleted?.not() ?: true
    val avatarColor = getAvatarColor(user.name)

    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            title = if (isActive)
                stringResource(R.string.dialog_title_disable_user)
            else
                stringResource(R.string.dialog_title_enable_user),
            message = if (isActive)
                stringResource(R.string.dialog_message_disable_user, user.name)
            else
                stringResource(R.string.dialog_message_enable_user, user.name),
            onConfirm = {
                coroutineScope.launch {
                    try {
                        // val success = userRepository.softDeleteUser(user.id)
                        // if (success) {
                        //     onRefresh()
                        // } else {
                        //     Log.e("UsersActivity", "Failed to toggle user status")
                        // }
                    } catch (e: Exception) {
                        Log.e("UsersActivity", "Error toggling user status: ${e.message}")
                    }
                }
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(avatarColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDBEAFE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.role_user),
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isActive) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isActive) Color(0xFF059669) else Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(if (isActive) R.string.status_active else R.string.status_inactive),
                            fontSize = 12.sp,
                            color = if (isActive) Color(0xFF059669) else Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = stringResource(if (isActive) R.string.action_disable else R.string.action_enable),
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(context, UserDetailActivity::class.java).apply {
                            putExtra("userId", user.id)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.action_details),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFF3B82F6)
    )
    return colors[name.hashCode().mod(colors.size)]
}