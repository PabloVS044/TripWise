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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.components.AppLogoHeader

data class InterestItem(
    val name: String,
    val icon: ImageVector,
    val key: String
)

@Composable
fun InterestsScreen(
    selectedInterests: Set<String>,
    onInterestsChanged: (Set<String>) -> Unit,
    totalSteps: Int = 3, // Added parameter
    modifier: Modifier = Modifier
) {
    val availableInterests = listOf(
        InterestItem(stringResource(R.string.adventure), Icons.Default.Hiking, "aventura"),
        InterestItem(stringResource(R.string.beach), Icons.Default.BeachAccess, "playa"),
        InterestItem(stringResource(R.string.mountain), Icons.Default.Terrain, "montaña"),
        InterestItem(stringResource(R.string.city), Icons.Default.LocationCity, "ciudad"),
        InterestItem(stringResource(R.string.culture), Icons.Default.Museum, "cultura"),
        InterestItem(stringResource(R.string.gastronomy), Icons.Default.Restaurant, "gastronomia"),
        InterestItem(stringResource(R.string.history), Icons.Default.Castle, "historia"),
        InterestItem(stringResource(R.string.nature), Icons.Default.Park, "naturaleza"),
        InterestItem(stringResource(R.string.relax), Icons.Default.Spa, "relax"),
        InterestItem(stringResource(R.string.photography), Icons.Default.CameraAlt, "fotografia")
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
            AppLogoHeader(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }

        item {
            Text(
                text = stringResource(R.string.choose_account_message),
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
                    currentStep = if (totalSteps == 4) 4 else 3,
                    totalSteps = totalSteps
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.your_interests),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = stringResource(R.string.choose_interests_message),
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