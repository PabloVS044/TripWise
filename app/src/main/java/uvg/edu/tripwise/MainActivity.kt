package uvg.edu.tripwise

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                TripWiseLandingPage(
                    onLoginClick = {
                        val intent = Intent(this, UsersActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripWiseLandingPage(onLoginClick: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEBF4FF),
                        Color.White,
                        Color(0xFFF3E8FF)
                    )
                )
            )
    ) {
        item { TopAppBarSection(onLoginClick = onLoginClick) }
        item { HeroSection() }
        item { FeaturesSection() }
        item { HowItWorksSection() }
        item { TestimonialsSection() }
        item { CTASection() }
        item { FooterSection() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarSection(onLoginClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "TripWise",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        },
        actions = {
            TextButton(onClick = onLoginClick) {
                Text("Iniciar Sesión", color = Color(0xFF2563EB))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun HeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge
        Surface(
            modifier = Modifier.padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFDBEAFE)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Powered by AI",
                    color = Color(0xFF1E40AF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Main Title
        Text(
            text = "Viaja Inteligente,\nVive Experiencias",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 42.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subtitle
        Text(
            text = "Descubre alojamientos únicos y recibe itinerarios personalizados creados por IA. TripWise combina las mejores reservas con recomendaciones inteligentes.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* TODO: Start Adventure */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Comenzar Aventura",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* TODO: Demo */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Ver Demo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF374151))
            }
        }

        // Stats
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("50K+", "Viajeros Felices")
            StatItem("1M+", "Itinerarios Creados")
            StatItem("4.9★", "Calificación")
        }
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Section Header
        Surface(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF3E8FF)
        ) {
            Text(
                "Características Principales",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFF7C2D92),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Todo lo que necesitas para viajar mejor",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(
            text = "Combinamos la mejor tecnología de reservas con inteligencia artificial",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // Features Grid
        val features = listOf(
            Feature(Icons.Default.Home, "Alojamientos Únicos", "Descubre desde casas locales hasta hoteles boutique", Color(0xFFDBEAFE), Color(0xFF2563EB)),
            Feature(Icons.Default.SmartToy, "IA Personalizada", "Itinerarios únicos basados en tu destino e intereses", Color(0xFFF3E8FF), Color(0xFF7C3AED)),
            Feature(Icons.Default.LocationOn, "Recomendaciones Locales", "Sugerencias auténticas que solo los locales conocen", Color(0xFFD1FAE5), Color(0xFF059669)),
            Feature(Icons.Default.Schedule, "Planificación Inteligente", "Optimizamos tu tiempo con rutas eficientes", Color(0xFFFED7AA), Color(0xFFEA580C)),
            Feature(Icons.Default.Favorite, "Experiencias Auténticas", "Conecta con la cultura local", Color(0xFFFCE7F3), Color(0xFFDB2777)),
            Feature(Icons.Default.Support, "Soporte 24/7", "Asistencia completa durante tu viaje", Color(0xFFE0E7FF), Color(0xFF4F46E5))
        )

        features.chunked(2).forEach { rowFeatures ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCard(
                        feature = feature,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class Feature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val iconColor: Color
)

@Composable
fun FeatureCard(feature: Feature, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(feature.backgroundColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = feature.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = feature.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = feature.description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFEBF4FF), Color(0xFFF3E8FF))
                )
            )
            .padding(24.dp)
    ) {
        // Section Header
        Surface(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFDBEAFE)
        ) {
            Text(
                "Proceso Simple",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFF1E40AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Cómo funciona TripWise",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(
            text = "En solo 3 pasos simples, tendrás tu alojamiento perfecto y un itinerario personalizado",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // Steps
        val steps = listOf(
            Step("1", "Busca y Reserva", "Encuentra el alojamiento perfecto para tu destino", listOf(Color(0xFF2563EB), Color(0xFF7C3AED))),
            Step("2", "IA Crea tu Itinerario", "Nuestro agente inteligente crea un itinerario personalizado", listOf(Color(0xFF7C3AED), Color(0xFFDB2777))),
            Step("3", "Disfruta tu Viaje", "Sigue tu itinerario y descubre experiencias únicas", listOf(Color(0xFFDB2777), Color(0xFFDC2626)))
        )

        steps.forEach { step ->
            StepItem(step)
            if (step != steps.last()) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class Step(
    val number: String,
    val title: String,
    val description: String,
    val gradientColors: List<Color>
)

@Composable
fun StepItem(step: Step) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Brush.horizontalGradient(step.gradientColors),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = step.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = step.description,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun TestimonialsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Section Header
        Surface(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFD1FAE5)
        ) {
            Text(
                "Testimonios",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFF065F46),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Lo que dicen nuestros viajeros",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        val testimonials = listOf(
            Testimonial("María González", "Viajera Frecuente", "TripWise transformó completamente mi viaje a Guatemala. El itinerario generado por IA me llevó a lugares increíbles.", "M", Color(0xFFDBEAFE), Color(0xFF2563EB)),
            Testimonial("Carlos Mendoza", "Aventurero Digital", "La combinación de alojamientos únicos con itinerarios personalizados es genial. Cada recomendación fue perfecta.", "C", Color(0xFFF3E8FF), Color(0xFF7C3AED)),
            Testimonial("Ana Rodríguez", "Madre de Familia", "Como familia, necesitábamos actividades para todos. TripWise creó el itinerario perfecto para toda la semana.", "A", Color(0xFFD1FAE5), Color(0xFF059669))
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(testimonials) { testimonial ->
                TestimonialCard(testimonial)
            }
        }
    }
}

data class Testimonial(
    val name: String,
    val role: String,
    val content: String,
    val initial: String,
    val backgroundColor: Color,
    val textColor: Color
)

@Composable
fun TestimonialCard(testimonial: Testimonial) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Stars
            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                repeat(5) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = testimonial.content,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(testimonial.backgroundColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = testimonial.initial,
                        fontWeight = FontWeight.Bold,
                        color = testimonial.textColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = testimonial.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = testimonial.role,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun CTASection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Listo para tu próxima aventura inteligente?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Únete a miles de viajeros que ya descubrieron la magia de viajar con TripWise. Tu próximo destino te está esperando.",
            fontSize = 16.sp,
            color = Color(0xFFBFDBFE),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        var email by remember { mutableStateOf("") }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Ingresa tu email", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { /* TODO: Sign up */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Comenzar Gratis",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF2563EB)
            )
        }

        Text(
            text = "Sin tarjeta de crédito requerida • Cancela cuando quieras",
            fontSize = 12.sp,
            color = Color(0xFFBFDBFE),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111827))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        ),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "TripWise",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "Viaja inteligente, vive experiencias únicas con el poder de la inteligencia artificial.",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Divider(
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "© 2024 TripWise. Todos los derechos reservados.",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTripWiseLanding() {
    TripWiseTheme {
        TripWiseLandingPage()
    }
}
