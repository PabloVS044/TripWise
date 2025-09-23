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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

    var selectedRoom by remember { mutableStateOf("deluxe") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SmallTopApp(
                title = { Text(stringResource(R.string.app_name), color = Color(0xFF0066CC), fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Column {
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
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color(0xFF1E40AF))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.back))
                        }

                        Button(
                            onClick = { /* TODO: Next page */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(stringResource(R.string.next), color = Color.White)
                        }
                    }
                }

                NavigationBar(containerColor = Color(0xFFF7F0F7)) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_desc)) },
                        label = { Text(stringResource(R.string.search)) },
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
                        icon = { Icon(Icons.Default.Luggage, contentDescription = stringResource(R.string.reservation_desc)) },
                        label = { Text(stringResource(R.string.reservation)) },
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
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile_desc)) },
                        label = { Text(stringResource(R.string.profile)) },
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
        Box(
            modifier = Modifier
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
                    text = stringResource(R.string.choose_accommodation),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Habitación estándar
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
                                Text(stringResource(R.string.room_standard), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text(stringResource(R.string.price_standard), fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.description_standard), fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción para Vista jardín */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.view_garden)) }

                            OutlinedButton(
                                onClick = { /* Acción para Free WiFi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.free_wifi)) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(stringResource(R.string.air_conditioning), fontSize = 12.sp, color = Color(0xFF1E40AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Habitación Deluxe
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
                                Text(stringResource(R.string.room_deluxe), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text(stringResource(R.string.price_deluxe), fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.description_deluxe), fontSize = 14.sp, color = Color.Gray)
                                Text(stringResource(R.string.per_person), fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción Vida al mar */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.sea_view)) }

                            OutlinedButton(
                                onClick = { /* Acción Balcón */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.balcony)) }

                            OutlinedButton(
                                onClick = { /* Acción Free WiFi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.free_wifi)) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(stringResource(R.string.mini_bar), fontSize = 12.sp, color = Color(0xFF1E40AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Habitación Premium
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
                                Text(stringResource(R.string.room_premium), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Text(stringResource(R.string.price_premium), fontSize = 16.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.description_premium), fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /* Acción Terraza privada */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.private_terrace)) }

                            OutlinedButton(
                                onClick = { /* Acción Jacuzzi */ },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E40AF))
                            ) { Text(stringResource(R.string.jacuzzi)) }
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
