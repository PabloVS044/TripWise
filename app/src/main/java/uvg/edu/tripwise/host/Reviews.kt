package uvg.edu.tripwise.host.reviews

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.PropertyReviews
import uvg.edu.tripwise.data.model.ReviewItem

private val PrimaryBlue = Color(0xFF2563EB)
private val StarYellow = Color(0xFFFFB800)

@Composable
fun ReviewsSection(
    propertyId: String,
    viewModel: ReviewsViewModel,
    reloadKey: Int
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(propertyId, reloadKey) {
        viewModel.load(propertyId)
    }

    when {
        state.loading -> {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }

        state.error != null -> {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        R.string.error_with_message,
                        state.error ?: ""
                    ),
                    color = Color(0xFFE2265B),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.load(propertyId) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }

        state.data != null -> ReviewsContent(state.data!!)
    }
}

@Composable
private fun ReviewsContent(data: PropertyReviews) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.reviews_title),
            color = PrimaryBlue,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                String.format("%.1f", data.averageScore),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF102A43)
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = StarYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    R.string.reviews_count,
                    data.totalReviews
                ),
                fontSize = 16.sp,
                color = Color(0xFF50607A)
            )
        }
        Spacer(Modifier.height(12.dp))

        ScoreDistributionBlock(dist = data.scoreDistribution)
        Spacer(Modifier.height(12.dp))

        Divider(thickness = 1.dp, color = PrimaryBlue.copy(alpha = 0.15f))
        Spacer(Modifier.height(12.dp))
        data.reviews.forEachIndexed { idx, review ->
            ReviewCard(review)
            if (idx < data.reviews.lastIndex) Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ScoreDistributionBlock(
    dist: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (score in 5 downTo 1) {
            val count = dist[score] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$score",
                    fontSize = 14.sp,
                    color = Color(0xFF102A43),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reviews_score_count, count),
                    fontSize = 14.sp,
                    color = Color(0xFF50607A)
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItem) {
    val context = LocalContext.current
    val dateText = remember(review.date) { formatDate(review.date, context) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = review.userAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            review.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF102A43)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            repeat(5) { i ->
                                Icon(
                                    imageVector = if (i < review.score)
                                        Icons.Filled.Star
                                    else
                                        Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = StarYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = Color(0xFF50607A),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            review.commentText?.let { text ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color(0xFF26364D),
                    lineHeight = 20.sp
                )
            }

            if (review.commentsCount > 0) {
                Spacer(Modifier.height(12.dp))
                val commentsText =
                    if (review.commentsCount == 1)
                        stringResource(R.string.review_one_comment)
                    else
                        stringResource(
                            R.string.review_multiple_comments,
                            review.commentsCount
                        )

                Text(
                    text = commentsText,
                    fontSize = 13.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatDate(dateString: String, context: Context): String {
    return try {
        val parts = dateString.split("T")
        if (parts.isNotEmpty()) {
            val d = parts[0].split("-")
            if (d.size == 3) {
                val y = d[0].toInt()
                val m = d[1].toInt()
                val dd = d[2].toInt()
                val days = calculateDaysAgo(y, m, dd)
                val res = context.resources
                when {
                    days == 0 -> res.getString(R.string.review_time_today)
                    days == 1 -> res.getString(R.string.review_time_one_day)
                    days < 7 -> res.getString(R.string.review_time_days, days)
                    days < 14 -> res.getString(R.string.review_time_one_week)
                    days < 30 -> res.getString(
                        R.string.review_time_weeks,
                        days / 7
                    )

                    days < 60 -> res.getString(R.string.review_time_one_month)
                    else -> res.getString(
                        R.string.review_time_months,
                        days / 30
                    )
                }
            } else dateString
        } else dateString
    } catch (_: Exception) {
        dateString
    }
}

private fun calculateDaysAgo(year: Int, month: Int, day: Int): Int {
    val currentYear = 2025
    val currentMonth = 11
    val currentDay = 8
    return maxOf(
        0,
        (currentYear - year) * 365 +
                (currentMonth - month) * 30 +
                (currentDay - day)
    )
}
