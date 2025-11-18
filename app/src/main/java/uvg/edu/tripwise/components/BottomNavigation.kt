package uvg.edu.tripwise.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.admin.DashboardActivity
import uvg.edu.tripwise.admin.PropertiesActivity
import uvg.edu.tripwise.admin.UsersActivity

@Composable
fun BottomNavigation(context: Context, currentScreen: String = "") {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2563EB),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dashboardTitle = stringResource(R.string.dashboard)
            val usersTitle = stringResource(R.string.nav_users)
            val propertiesTitle = stringResource(R.string.properties_title)

            CustomBottomNavItem(
                title = dashboardTitle,
                icon = Icons.Default.Dashboard,
                isSelected = currentScreen == dashboardTitle,
                onClick = {
                    context.startActivity(Intent(context, DashboardActivity::class.java))
                }
            )
            CustomBottomNavItem(
                title = usersTitle,
                icon = Icons.Default.People,
                isSelected = currentScreen == usersTitle,
                onClick = {
                    context.startActivity(Intent(context, UsersActivity::class.java))
                }
            )
            CustomBottomNavItem(
                title = propertiesTitle,
                icon = Icons.Default.Home,
                isSelected = currentScreen == propertiesTitle,
                onClick = {
                    context.startActivity(Intent(context, PropertiesActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun CustomBottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}