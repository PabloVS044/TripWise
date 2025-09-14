package uvg.edu.tripwise.ui.components  // Adjust package if needed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R  // Import your R file

@Composable
fun AppLogoHeader(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,  // Default medium size
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color(0xFF2563EB)
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logotripwise),  // Replace with your actual drawable ID, e.g., R.drawable.ic_logo
                contentDescription = stringResource(R.string.app_name),  // Accessibility
                modifier = Modifier.size(fontSize.value.dp * 2.2f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color
            )
        }
    }
}