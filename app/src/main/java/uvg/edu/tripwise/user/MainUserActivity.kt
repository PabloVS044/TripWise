package uvg.edu.tripwise.user

import android.content.Intent
import uvg.edu.tripwise.MainActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class MainUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                MainUserScreen(
                    onLogout = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUserScreen(
    onLogout: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Panel de Usuario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenido a tu área personal de TripWise.\nAquí podrás explorar propiedades, hacer reservas\ny gestionar tus viajes.",
                fontSize = 16.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rol: Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF059669)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Como usuario puedes navegar por las propiedades disponibles, realizar reservas y gestionar tus viajes de manera fácil y segura.",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Logout"
            )
        }
    }
}