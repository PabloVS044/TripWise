package uvg.edu.tripwise.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.admin.PropertyDetailActivity
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository

@Composable
fun PropertyCard(
    property: Property,
    onRefresh: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val propertyRepository = remember { PropertyRepository() }
    val context = LocalContext.current

    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            title = if (property.deleted.isDeleted) "Enable Property" else "Disable Property",
            message = if (property.deleted.isDeleted)
                "Are you sure you want to enable ${property.name}? It will be visible to users again."
            else
                "Are you sure you want to disable ${property.name}? It will be hidden from users.",
            onConfirm = {
                coroutineScope.launch {
                    try {
                        val success = propertyRepository.deleteProperty(property.id)
                        if (success) {
                            onRefresh()
                        } else {
                            Log.e("PropertiesActivity", "Failed to toggle property status")
                        }
                    } catch (e: Exception) {
                        Log.e("PropertiesActivity", "Error toggling property status", e)
                    }
                }
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = property.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = property.location,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (property.approved.lowercase()) {
                        "approved" -> Color(0xFFD1FAE5)
                        "pending" -> Color(0xFFFEF3C7)
                        "rejected" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFE5E7EB)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (property.approved.lowercase()) {
                                "approved" -> Icons.Default.Check
                                "pending" -> Icons.Default.Schedule
                                "rejected" -> Icons.Default.Close
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (property.approved.lowercase()) {
                                "approved" -> Color(0xFF059669)
                                "pending" -> Color(0xFFD97706)
                                "rejected" -> Color(0xFFDC2626)
                                else -> Color(0xFF6B7280)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = property.approved.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            color = when (property.approved.lowercase()) {
                                "approved" -> Color(0xFF059669)
                                "pending" -> Color(0xFFD97706)
                                "rejected" -> Color(0xFFDC2626)
                                else -> Color(0xFF6B7280)
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (property.pictures.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(property.pictures.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = property.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.ic_menu_gallery)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "No image",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDBEAFE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = property.propertyType,
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFCE7F3)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFDB2777),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${property.capacity} guests",
                            fontSize = 12.sp,
                            color = Color(0xFFDB2777),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD1FAE5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$${property.pricePerNight}/night",
                            fontSize = 12.sp,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = if (property.deleted.isDeleted) "Enable" else "Disable",
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(context, PropertyDetailActivity::class.java).apply {
                            putExtra("propertyId", property.id)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Text(
                        text = "Details",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}