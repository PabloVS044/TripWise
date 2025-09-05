package uvg.edu.tripwise.auth.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.auth.steps.StepIndicator

data class InterestItem(
    val name: String,
    val icon: ImageVector,
    val key: String
)

@Composable
fun InterestsScreen(
    selectedInterests: Set<String>,
    onInterestsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableInterests = listOf(
        InterestItem("Aventura", Icons.Default.Hiking, "aventura"),
        InterestItem("Playa", Icons.Default.BeachAccess, "playa"),
        InterestItem("Montaña", Icons.Default.Terrain, "montaña"),
        InterestItem("Ciudad", Icons.Default.LocationCity, "ciudad"),
        InterestItem("Cultura", Icons.Default.Museum, "cultura"),
        InterestItem("Gastronomía", Icons.Default.Restaurant, "gastronomia"),
        InterestItem("Historia", Icons.Default.Castle, "historia"),
        InterestItem("Naturaleza", Icons.Default.Park, "naturaleza"),
        InterestItem("Relax", Icons.Default.Spa, "relax"),
        InterestItem("Fotografía", Icons.Default.CameraAlt, "fotografia")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) }

        item {
            Text(
                text = "TripWise",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Elige tu cuenta para comenzar tu próxima aventura",
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                StepIndicator(
                    currentStep = 3,
                    totalSteps = 3
                )
            }
        }

        item {
            Text(
                text = "Tus intereses",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Elige tus intereses para personalizar tu experiencia",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        items(availableInterests.chunked(2)) { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { interest ->
                    InterestCard(
                        interest = interest,
                        isSelected = selectedInterests.contains(interest.key),
                        onClick = {
                            val newInterests = if (selectedInterests.contains(interest.key)) {
                                selectedInterests - interest.key
                            } else {
                                selectedInterests + interest.key
                            }
                            onInterestsChanged(newInterests)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InterestCard(
    interest: InterestItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = interest.icon,
                    contentDescription = interest.name,
                    tint = if (isSelected) Color.White else Color(0xFF6B7280),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = interest.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF2563EB) else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
