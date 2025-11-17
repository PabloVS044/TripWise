package uvg.edu.tripwise.host

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.repository.ReservationRepository
import uvg.edu.tripwise.network.ReservationResponse
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val PrimaryBlue  = Color(0xFF2563EB)
private val SuccessGreen = Color(0xFF0AA12E)
private val DangerRed    = Color(0xFFE2265B)

@Composable
fun ReservationsSection(
    propertyId: String,
    reloadKey: Int = 0
) {
    val repo = remember { ReservationRepository() }

    var isLoading by remember { mutableStateOf(false) }
    var error     by remember { mutableStateOf<String?>(null) }
    var items     by remember { mutableStateOf<List<ReservationResponse>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(-1) }
    val currentPropertyId by rememberUpdatedState(propertyId)
    LaunchedEffect(currentPropertyId, reloadKey) {
        isLoading = true
        error = null
        try {
            items = repo.getByProperty(currentPropertyId)
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    val reservations = remember(items) {
        items.sortedWith(compareBy { toDateSafe(it.checkInDate) ?: Date(Long.MAX_VALUE) })
    }
    val activeCount = reservations.count { it.state.equals("confirmed", ignoreCase = true) }
    val guestFallback = stringResource(R.string.guest_default)


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.your_bookings_title),
                color = PrimaryBlue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            PillTag(
                text = stringResource(R.string.active_count, activeCount),
                color = SuccessGreen
            )
        }
        Spacer(Modifier.height(14.dp))

        when {
            isLoading -> LoadingCard()
            error != null -> ErrorCard(error!!)
            reservations.isEmpty() -> EmptyCard(
                text = stringResource(R.string.no_bookings_yet)
            )
            else -> {
                reservations.forEachIndexed { index, r ->
                    ReservationItemBound(
                        name      = resolveGuestName(r, guestFallback),
                        dateRange = formatRange(r.checkInDate, r.checkOutDate),
                        amount    = formatMoney(r.payment),
                        state     = r.state,
                        persons   = r.persons,
                        selected  = index == selectedIndex,
                        onClick   = { selectedIndex = index }
                    )
                }
            }
        }
    }
}


@Composable
private fun PillTag(text: String, color: Color) {
    Surface(
        color = color,
        contentColor = Color.White,
        shape = RoundedCornerShape(50),
        shadowElevation = 0.dp
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReservationItemBound(
    name: String,
    dateRange: String,
    amount: String,
    state: String,
    persons: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(2.dp, PrimaryBlue)
    else BorderStroke(1.dp, Color(0xFFE8E8F0))

    val (statusColor, statusText) = when (state.lowercase(Locale.US)) {
        "confirmed" -> SuccessGreen      to stringResource(R.string.status_confirmed)
        "pending"   -> DangerRed         to stringResource(R.string.status_pending)
        "cancelled" -> Color(0xFF9CA3AF) to stringResource(R.string.status_cancelled)
        else        -> Color(0xFF9CA3AF) to state
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border,
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102A43)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateRange, color = Color(0xFF6B7A90))
                    Spacer(Modifier.size(8.dp))
                    androidx.compose.material3.Icon(
                        Icons.Filled.People,
                        contentDescription = null,
                        tint = Color(0xFF6B7A90)
                    )
                    Text(" $persons", color = Color(0xFF6B7A90))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    amount,
                    color = SuccessGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                StatusChip(text = statusText, color = statusColor)
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(
        color = color,
        contentColor = Color.White,
        shape = RoundedCornerShape(50),
        shadowElevation = 0.dp
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingCard() {
    PlaceholderCard(text = stringResource(R.string.loading_ellipsis))
}

@Composable
private fun EmptyCard(text: String) {
    PlaceholderCard(text = text)
}

@Composable
private fun ErrorCard(message: String) {
    PlaceholderCard(text = stringResource(R.string.error_with_message, message))
}

@Composable
private fun PlaceholderCard(text: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color(0xFF50607A), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun resolveGuestName(
    r: ReservationResponse,
    fallback: String
): String {
    return try {
        if (r.reservationUser is uvg.edu.tripwise.network.ApiUser) {
            r.reservationUser.name
        } else {
            @Suppress("UNCHECKED_CAST")
            (r.reservationUser as? Map<*, *>)?.get("name") as? String ?: fallback
        }
    } catch (_: Throwable) {
        fallback
    }
}

private fun toDateSafe(raw: String?): Date? {
    if (raw.isNullOrBlank()) return null
    raw.toLongOrNull()?.let { ms ->
        val minMs = 946684800000L
        return if (ms >= minMs) Date(ms) else null
    }
    val utc = TimeZone.getTimeZone("UTC")
    val f1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = utc }
    val f2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",       Locale.US).apply { timeZone = utc }
    return try { f1.parse(raw) } catch (_: ParseException) {
        try { f2.parse(raw) } catch (_: ParseException) { null }
    }
}

private fun formatRange(ci: String?, co: String?): String {
    val d1 = toDateSafe(ci) ?: return "—"
    val d2 = toDateSafe(co) ?: return "—"
    val mon = SimpleDateFormat("MMM", Locale.ENGLISH)
    val day = SimpleDateFormat("d", Locale.US)
    val m1 = mon.format(d1); val m2 = mon.format(d2)
    val a1 = day.format(d1); val a2 = day.format(d2)
    return if (m1 == m2) "$m1 $a1–$a2" else "$m1 $a1 – $m2 $a2"
}

private fun formatMoney(value: Any?): String {
    val v = (value as? Number)?.toDouble() ?: 0.0
    return NumberFormat.getCurrencyInstance(Locale.US).format(v)
}
