package uvg.edu.tripwise.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.R
import uvg.edu.tripwise.auth.SessionManager

@Composable
fun LogoAppTopBar(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    Surface(shadowElevation = 4.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp)
                .clipToBounds(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .clipToBounds()
            ) {
                AppLogoHeader(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer { scaleX = 0.70f; scaleY = 0.70f }
                )
            }

            IconButton(
                onClick = {
                    onLogout()

                    sessionManager.clearSession()

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.cd_logout),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}