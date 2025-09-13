package uvg.edu.tripwise.admin

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.components.BottomNavigation
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.data.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var properties by remember { mutableStateOf<List<uvg.edu.tripwise.data.model.Property>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val userRepository = remember { UserRepository() }
    val propertyRepository = remember { PropertyRepository() }

    // Estadísticas calculadas
    val totalUsers = users.size
    val activeUsers = users.count { it.deleted?.isDeleted?.not() ?: true }
    val inactiveUsers = users.count { it.deleted?.isDeleted ?: false }

    val totalProperties = properties.size
    val approvedProperties = properties.count { it.approved.equals("approved", ignoreCase = true) }
    val pendingProperties = properties.count { it.approved.equals("pending", ignoreCase = true) }
    val rejectedProperties = properties.count { it.approved.equals("rejected", ignoreCase = true) }

    fun loadData() {
        scope.launch {
            try {
                isLoading = true
                users = userRepository.getUsers()
                properties = propertyRepository.getProperties()
                Log.d("Dashboard", "Data loaded: $totalUsers users, $totalProperties properties")
            } catch (e: Exception) {
                Log.e("Dashboard", "Error loading data", e)
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun refreshData() {
        isRefreshing = true
        loadData()
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Status Bar
        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = stringResource(R.string.dashboard),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { refreshData() },
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading && !isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Statistics Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.total_users),
                            value = totalUsers.toString(),
                            icon = Icons.Default.People,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.total_properties),
                            value = totalProperties.toString(),
                            icon = Icons.Default.Home,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Users Overview
                    OverviewCard(
                        title = stringResource(R.string.users_overview),
                        icon = Icons.Default.Person,
                        items = listOf(
                            StatItem(stringResource(R.string.active_users), activeUsers.toString(), Color(0xFF10B981)),
                            StatItem(stringResource(R.string.inactive_users), inactiveUsers.toString(), Color(0xFFEF4444))
                        )
                    )

                    // Properties Overview
                    OverviewCard(
                        title = stringResource(R.string.properties_status),
                        icon = Icons.Default.Home,
                        items = listOf(
                            StatItem(stringResource(R.string.approved), approvedProperties.toString(), Color(0xFF10B981)),
                            StatItem(stringResource(R.string.pending), pendingProperties.toString(), Color(0xFFF59E0B)),
                            StatItem(stringResource(R.string.rejected), rejectedProperties.toString(), Color(0xFFEF4444))
                        )
                    )

                    // Quick Actions
                    QuickActionsCard()

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Bottom Navigation
        BottomNavigation(context = context, currentScreen = "Dashboard")
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<StatItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = item.value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = item.color
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_actions),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = stringResource(R.string.manage_users),
                    icon = Icons.Default.People,
                    onClick = {
                        context.startActivity(Intent(context, UsersActivity::class.java))
                    }
                )
                ActionButton(
                    text = stringResource(R.string.manage_properties),
                    icon = Icons.Default.Home,
                    onClick = {
                        context.startActivity(Intent(context, PropertiesActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

data class StatItem(
    val label: String,
    val value: String,
    val color: Color
)