package uvg.edu.tripwise.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.admin.PropertiesActivity
import uvg.edu.tripwise.admin.UsersActivity

@Composable
fun BottomNavigation(context: Context, currentScreen: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        // Gradiente decorativo superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF2563EB),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CustomBottomNavItem(
                title = "Usuarios",
                icon = Icons.Default.People,
                isSelected = currentScreen == "Users",
                onClick = {
                    context.startActivity(Intent(context, UsersActivity::class.java))
                }
            )

            CustomBottomNavItem(
                title = "Propiedades",
                icon = Icons.Default.Home,
                isSelected = currentScreen == "Properties",
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .then(
                if (isSelected) {
                    Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Fondo del item seleccionado
        if (isSelected) {
            Surface(
                modifier = Modifier.size(width = 100.dp, height = 56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2563EB).copy(alpha = 0.1f)
            ) {}
        }

        Column(
            horizontalAlignment = Alignment.CenterVertically as Alignment.Horizontal,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Contenedor del icono
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF2563EB)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            )
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color.White else Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Texto animado
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF6B7280),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}