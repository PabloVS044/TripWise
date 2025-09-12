package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class ReservationPage2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                ReservaScreen2()
            }
        }
    }
}

@Composable
@Preview
fun ReservaScreen2() {
    val context = LocalContext.current

    // Estado para la selección de habitación
    var selectedRoom by remember { mutableStateOf("deluxe") } // Por defecto "deluxe" como en la imagen

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SmallTopApp(
                title = { Text("Tripwise", color = Color(0xFF0066CC), fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Column {
                // Botones "Atrás" y "Siguiente" en un BottomAppBar
                BottomAppBar(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E40AF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val intent = Intent(context, ReservationPage1Activity::class.java)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E40AF))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF1E40AF))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atrás")
                        }

                        Button(
                            onClick = {
                                // TODO: Navegar a la siguiente vista (ReservationPage3Activity)
                                // val intent = Intent(context, ReservationPage3Activity::class.java)
                                // context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Siguiente", color = Color.White)
                        }
                    }
                }
                // Barra de navegación
                NavigationBar(containerColor = Color(0xFFF7F0F7)) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        label = { Text("Search") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, DiscoverActivity::class.java)
                            context.startActivity(intent)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1976D2),
                            selectedTextColor = Color(0xFF1976D2),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Luggage, contentDescription = "Reservacion") },
                        label = { Text("Reservacion") },
                        selected = true,
                        onClick = {},
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1976D2),
                            selectedTextColor = Color(0xFF1976D2),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Profile") },
                        selected = false,
                        onClick = {},
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1976D2),
                            selectedTextColor = Color(0xFF1976D2),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Elige tu alojamiento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opción 1: Estándar
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (selectedRoom == "standard") Color(0xFF1E40AF) else Color.LightGray,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRoom == "standard",
                                onClick = { selectedRoom = "standard" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF1E40AF),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Habitación estándar", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text("Q1,500 por persona", fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text("Habitación cómoda con jardín", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción para Vida jardín */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Vista jardín")
                            }
                            OutlinedButton(
                                onClick = { /* Acción para Free WiFi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Free WiFi")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Incluye Aire acondicionado", fontSize = 12.sp, color = Color(0xFF1E40AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opción 2: Deluxe
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (selectedRoom == "deluxe") Color(0xFF1E40AF) else Color.LightGray,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRoom == "deluxe",
                                onClick = { selectedRoom = "deluxe" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF1E40AF),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Habitación Deluxe", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text("Q2,500", fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text("Habitación espaciosa con vista al mar", fontSize = 14.sp, color = Color.Gray)
                                Text("Por persona", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción para Vida al mar */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Vida al mar")
                            }
                            OutlinedButton(
                                onClick = { /* Acción para Balcón */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Balcón")
                            }
                            OutlinedButton(
                                onClick = { /* Acción para Free WiFi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Free WiFi")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Mini Bar", fontSize = 12.sp, color = Color(0xFF1E40AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opción 3: Premium
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (selectedRoom == "premium") Color(0xFF1E40AF) else Color.LightGray,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRoom == "premium",
                                onClick = { selectedRoom = "premium" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF1E40AF),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Habitación premium", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text("Q4,000", fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text("Gran habitación con terraza privada", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción para Terraza privada */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Terraza privada")
                            }
                            OutlinedButton(
                                onClick = { /* Acción para Jacuzzi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) {
                                Text("Jacuzzi")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SmallTopApp(title: @Composable () -> Unit) {}