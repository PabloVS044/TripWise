package uvg.edu.tripwise.review

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.tripwise.auth.SessionManager
import uvg.edu.tripwise.network.CreateReviewRequest
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class CreateReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val propertyId = intent.getStringExtra("propertyId") ?: ""
        val propertyName = intent.getStringExtra("propertyName") ?: ""
        val propertyImage = intent.getStringExtra("propertyImage") ?: ""
        val propertyLocation = intent.getStringExtra("propertyLocation") ?: ""

        setContent {
            TripWiseTheme {
                CreateReviewScreen(
                    propertyId = propertyId,
                    propertyName = propertyName,
                    propertyImage = propertyImage,
                    propertyLocation = propertyLocation,
                    onReviewCreated = {
                        finish()
                    },
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    propertyId: String,
    propertyName: String,
    propertyImage: String,
    propertyLocation: String,
    onReviewCreated: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var rating by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dejar Reseña", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card de la propiedad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (propertyImage.isNotEmpty()) {
                            AsyncImage(
                                model = propertyImage,
                                contentDescription = propertyName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        Text(
                            text = propertyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = propertyLocation,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card de calificación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Cómo fue tu experiencia?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Tu opinión nos ayuda a mejorar",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Estrellas de calificación
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 1..5) {
                                IconButton(
                                    onClick = { rating = i },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "Estrella $i",
                                        tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }

                        if (rating > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (rating) {
                                    1 -> "Muy malo"
                                    2 -> "Malo"
                                    3 -> "Regular"
                                    4 -> "Bueno"
                                    5 -> "Excelente"
                                    else -> ""
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E88E5)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de enviar
                Button(
                    onClick = {
                        if (rating == 0) {
                            Toast.makeText(context, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val userId = sessionManager.getUserId()
                        if (userId.isNullOrEmpty()) {
                            Toast.makeText(context, "Sesión expirada", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSubmitting = true

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val request = CreateReviewRequest(
                                    userId = userId,
                                    propertyId = propertyId,
                                    score = rating
                                )

                                val response = RetrofitInstance.ReviewApi.createReview(request)

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "¡Reseña enviada exitosamente!", Toast.LENGTH_LONG).show()
                                        onReviewCreated()
                                    } else {
                                        Toast.makeText(context, "Error al enviar reseña: ${response.message()}", Toast.LENGTH_LONG).show()
                                        isSubmitting = false
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSubmitting && rating > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviar Reseña",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
