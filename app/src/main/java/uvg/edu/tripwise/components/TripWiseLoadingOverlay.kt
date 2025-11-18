package uvg.edu.tripwise.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TripWiseLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.loading_experience)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingAnimation")

    val planeAngle by infiniteTransition.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "planeRotation"
    )

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textFade"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2563EB),
                        Color(0xFF1E40AF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Canvas(modifier = Modifier.size(180.dp)) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2

                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.3f to Color.White.copy(alpha = 0.1f),
                            0.6f to Color.White.copy(alpha = 0.4f),
                            0.85f to Color.White.copy(alpha = 0.7f),
                            1f to Color.White,
                            center = Offset(centerX, centerY)
                        ),
                        startAngle = planeAngle - 120f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.logotripwise),
                    contentDescription = stringResource(R.string.cd_tripwise_logo),
                    modifier = Modifier.size(70.dp)
                )

                val radiusPixels = 90f
                val angleInRadians = Math.toRadians(planeAngle.toDouble())
                val planeX = (radiusPixels * cos(angleInRadians)).toFloat()
                val planeY = (radiusPixels * sin(angleInRadians)).toFloat()

                Box(
                    modifier = Modifier
                        .offset(x = planeX.dp, y = planeY.dp)
                        .graphicsLayer {
                            rotationZ = planeAngle + 180f
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_plane),
                        contentDescription = stringResource(R.string.cd_plane),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = textAlpha),
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                easing = FastOutSlowInEasing,
                                delayMillis = index * 200
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )

                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = dotAlpha),
                            radius = size.minDimension / 2
                        )
                    }
                }
            }
        }
    }
}