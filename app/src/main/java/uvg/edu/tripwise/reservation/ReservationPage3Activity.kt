package uvg.edu.tripwise.reservation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class ReservationPage3Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recibe el número de viajeros desde ReservationPage2Activity
        val numTravelers = intent.getIntExtra("numTravelers", 1)

        setContent {
            TripWiseTheme {
                ReservationPage3Screen(numTravelers)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationPage3Screen(numTravelers: Int) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Datos dinámicos para cada viajero
    val names = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val lastNames = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val emails = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }
    val phones = remember { mutableStateListOf<String>().apply { repeat(numTravelers) { add("") } } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traveler Information", color = Color.White) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF1E40AF))
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(context, ReservationPage2Activity::class.java)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E40AF))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E40AF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }

                    Button(
                        onClick = {
                            // Aquí puedes manejar el envío de los datos
                            // o mostrar un mensaje de confirmación
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                    ) {
                        Text("Confirm Reservation", color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Traveler Details",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Formulario dinámico para cada viajero
            repeat(numTravelers) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Traveler ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E40AF)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = names[index],
                            onValueChange = { names[index] = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = lastNames[index],
                            onValueChange = { lastNames[index] = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emails[index],
                            onValueChange = { emails[index] = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phones[index],
                            onValueChange = { phones[index] = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
