package uvg.edu.tripwise.host

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.tripwise.ui.theme.TripWiseTheme

enum class HostTab { Resumen, Reservas, Reseñas, Calendario }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesHost(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(HostTab.Resumen) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tripwise",
                        fontSize = 24.sp,
                        color = Color(0xFF1F47B2),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { inner ->
        Column(
            modifier = modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF7F7FB))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            StatCard("Ocupación", "85%", Color(0xFF2E63F1), Icons.Outlined.TrendingUp)
            Spacer(Modifier.height(10.dp))
            StatCard("Ingresos / mes", "$4500", Color(0xFF0AA12E), Icons.Outlined.AttachMoney)
            Spacer(Modifier.height(10.dp))
            StatCard("Calificación", "4.8", Color(0xFF8E198A), Icons.Outlined.StarOutline)
            Spacer(Modifier.height(10.dp))
            StatCard("Respuesta", "98%", Color(0xFFE2265B), Icons.Outlined.ChatBubbleOutline)

            Spacer(Modifier.height(16.dp))

            HostTopTabBar(selected = selectedTab, onSelected = { selectedTab = it })

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                HostTab.Resumen -> SummarySection()
                HostTab.Reservas -> PlaceholderSection("Reservas (pronto)")
                HostTab.Reseñas -> PlaceholderSection("Reseñas (pronto)")
                HostTab.Calendario -> PlaceholderSection("Calendario (pronto)")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, bg: Color, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White.copy(alpha = 0.92f), fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(value, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
        }
    }
}

@Composable
fun HostTopTabBar(
    selected: HostTab,
    onSelected: (HostTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = HostTab.values().toList()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1F47B2),
        shape = RoundedCornerShape(14.dp)
    ) {
        TabRow(
            selectedTabIndex = tabs.indexOf(selected),
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == selected,
                    onClick = { onSelected(tab) },
                    text = { Text(tab.name, color = Color.White, fontSize = 15.sp) }
                )
            }
        }
    }
}

@Composable
fun SummarySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhiteCard(title = "Información básica") {
            KeyValueRow("Tipo", "Casa completa")
            KeyValueRow("Huéspedes", "6 personas")
            KeyValueRow("Habitaciones", "3")
            KeyValueRow("Baños", "2")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Precio por noche:", fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
                Text("$150", fontWeight = FontWeight.Bold, color = Color(0xFF0AA12E))
            }
        }

        WhiteCard(title = "Amenidades") {
            AmenitiesGrid(
                listOf("WiFi gratuito", "Estacionamiento", "Cocina completa", "TV con Netflix")
            )
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF1F47B2)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Descripción", fontWeight = FontWeight.Bold, color = Color(0xFF1F47B2), fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hermosa casa frente al mar con vista panorámica al océano. " +
                            "Perfecta para familias que buscan relajarse en un ambiente tropical " +
                            "con todas las comodidades modernas.",
                    color = Color(0xFF102A43),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun WhiteCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFEFF)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8F0))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1F47B2), fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:", fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
        Text(value, color = Color(0xFF26364D))
    }
}

@Composable
fun AmenitiesGrid(items: List<String>) {
    val mid = (items.size + 1) / 2
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.take(mid).forEach { Text("• $it", color = Color(0xFF26364D)) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.drop(mid).forEach { Text("• $it", color = Color(0xFF26364D)) }
        }
    }
}

@Composable
fun PlaceholderSection(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = Color(0xFF50607A), textAlign = TextAlign.Center)
        }
    }
}


@Preview(
    name = "Host · Propiedades",
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun PropertiesHostPreview() {
    TripWiseTheme {
        PropertiesHost()
    }
}

@Preview(
    name = "Host · Dark",
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun PropertiesHostPreviewDark() {
    TripWiseTheme {
        PropertiesHost()
    }
}