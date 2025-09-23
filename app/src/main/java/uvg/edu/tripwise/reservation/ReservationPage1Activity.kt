package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.tripwise.R
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import androidx.compose.ui.res.stringResource

class ReservationPage1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                ReservaScreen()
            }
        }
    }
}

@Composable
fun ReservaScreen() {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.app_name), color = Color(0xFF0066CC), fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F0F7)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_desc)) },
                    label = { Text(stringResource(R.string.nav_search)) },
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
                    label = { Text(stringResource(R.string.nav_reservation)) },
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
                    label = { Text(stringResource(R.string.nav_profile)) },
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

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.sobre_viaje), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.hotel),
                                contentDescription = stringResource(R.string.santorini_greece),
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.santorini_greece), fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(stringResource(R.string.paradise_days), fontSize = 13.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.rating_reviews), fontSize = 12.sp, color = Color.Gray)
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(R.string.days_nights), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.detalles_viaje), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "15/12/2025",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.checkin_date), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Gray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "20/12/2025",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.checkout_date), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Gray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        var viajeros by remember { mutableStateOf(2) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.num_viajeros), fontSize = 14.sp, color = Color.Gray)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { if (viajeros > 1) viajeros-- },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                                ) { Text("-", color = Color.White) }
                                Spacer(Modifier.width(8.dp))
                                Text("$viajeros viajeros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viajeros++ },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                                ) { Text("+", color = Color.White) }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, ReservationPage2Activity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
            ) {
                Text(stringResource(R.string.siguiente), color = Color.White)
            }
        }
    }
}

@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {}
