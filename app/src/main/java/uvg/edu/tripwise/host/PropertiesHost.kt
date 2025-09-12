package uvg.edu.tripwise.host

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

/* Colores */
private val PrimaryBlue = Color(0xFF1F47B2)
private val SuccessGreen = Color(0xFF0AA12E)
private val DangerRed   = Color(0xFFE2265B)
private val PageBg      = Color(0xFFF7F7FB)

enum class HostTab(@StringRes val titleRes: Int) {
    Resumen(R.string.ph_tab_resumen),
    Reservas(R.string.ph_tab_reservas),
    Reseñas(R.string.ph_tab_resenas),
    Calendario(R.string.ph_tab_calendario)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesHost(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(HostTab.Resumen) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ph_app_title),
                        fontSize = 24.sp,
                        color = PrimaryBlue,
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
                .background(PageBg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            /* KPIs */
            StatCard(
                stringResource(R.string.ph_kpi_occupancy),
                stringResource(R.string.ph_kpi_occupancy_value),
                Color(0xFF2E63F1),
                Icons.Outlined.TrendingUp
            )
            Spacer(Modifier.height(10.dp))
            StatCard(
                stringResource(R.string.ph_kpi_income_month),
                stringResource(R.string.ph_kpi_income_value),
                SuccessGreen,
                Icons.Outlined.AttachMoney
            )
            Spacer(Modifier.height(10.dp))
            StatCard(
                stringResource(R.string.ph_kpi_rating),
                stringResource(R.string.ph_kpi_rating_value),
                Color(0xFF8E198A),
                Icons.Outlined.StarOutline
            )
            Spacer(Modifier.height(10.dp))
            StatCard(
                stringResource(R.string.ph_kpi_response),
                stringResource(R.string.ph_kpi_response_value),
                DangerRed,
                Icons.Outlined.ChatBubbleOutline
            )

            Spacer(Modifier.height(16.dp))

            HostTopTabBar(selected = selectedTab, onSelected = { selectedTab = it })

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                HostTab.Resumen -> SummarySection()
                HostTab.Reservas -> ReservationsSection()
                HostTab.Reseñas -> PlaceholderSection(stringResource(R.string.ph_placeholder_reviews))
                HostTab.Calendario -> PlaceholderSection(stringResource(R.string.ph_placeholder_calendar))
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
    val selectedIndex = tabs.indexOf(selected)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PrimaryBlue,
        shape = RoundedCornerShape(14.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .padding(horizontal = 28.dp),
                    height = 2.dp,
                    color = Color.White
                )
            },
            divider = {}
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == selected,
                    onClick = { onSelected(tab) },
                    text = { Text(stringResource(tab.titleRes), color = Color.White, fontSize = 15.sp) }
                )
            }
        }
    }
}

/* ===================== RESERVAS ===================== */

private enum class ReservationStatus { Confirmada, Pendiente }

private data class Reservation(
    val guestName: String,
    val dateRange: String,
    val amountUsd: String,
    val status: ReservationStatus
)

@Composable
private fun ReservationsSection() {
    var selectedIndex by remember { mutableStateOf(0) }
    val reservations = listOf(
        Reservation(
            guestName = stringResource(R.string.ph_res1_name),
            dateRange = stringResource(R.string.ph_res1_dates),
            amountUsd = stringResource(R.string.ph_res1_amount),
            status = ReservationStatus.Confirmada
        ),
        Reservation(
            guestName = stringResource(R.string.ph_res2_name),
            dateRange = stringResource(R.string.ph_res2_dates),
            amountUsd = stringResource(R.string.ph_res2_amount),
            status = ReservationStatus.Pendiente
        ),
        Reservation(
            guestName = stringResource(R.string.ph_res3_name),
            dateRange = stringResource(R.string.ph_res3_dates),
            amountUsd = stringResource(R.string.ph_res3_amount),
            status = ReservationStatus.Confirmada
        )
    )
    val activeCount = reservations.count { it.status == ReservationStatus.Confirmada }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.ph_reservations_title),
                color = PrimaryBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold
            )
            PillTag(
                text = pluralStringResource(
                    R.plurals.ph_reservations_active_count,
                    activeCount,
                    activeCount
                ),
                color = SuccessGreen
            )
        }

        reservations.forEachIndexed { index, r ->
            ReservationItem(
                reservation = r,
                selected = index == selectedIndex,
                onClick = { selectedIndex = index }
            )
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
private fun ReservationItem(
    reservation: Reservation,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(2.dp, PrimaryBlue) else BorderStroke(1.dp, Color(0xFFE8E8F0))
    val statusColor = if (reservation.status == ReservationStatus.Confirmada) SuccessGreen else DangerRed
    val statusText = if (reservation.status == ReservationStatus.Confirmada)
        stringResource(R.string.ph_reservation_status_confirmed)
    else
        stringResource(R.string.ph_reservation_status_pending)

    Card(
        shape = RoundedCornerShape(14.dp),
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    reservation.guestName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102A43)
                )
                Spacer(Modifier.height(2.dp))
                Text(reservation.dateRange, color = Color(0xFF6B7A90))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    reservation.amountUsd,
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
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ===================== RESUMEN ===================== */

@Composable
fun SummarySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhiteCard(title = stringResource(R.string.ph_section_basic_info)) {
            KeyValueRow(stringResource(R.string.ph_label_type), stringResource(R.string.ph_summary_type_value))
            KeyValueRow(stringResource(R.string.ph_label_guests), stringResource(R.string.ph_summary_guests_value))
            KeyValueRow(stringResource(R.string.ph_label_rooms), stringResource(R.string.ph_summary_rooms_value))
            KeyValueRow(stringResource(R.string.ph_label_bathrooms), stringResource(R.string.ph_summary_bathrooms_value))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.ph_label_price_per_night),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102A43)
                )
                Text(stringResource(R.string.ph_summary_price_value), fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
        }

        WhiteCard(title = stringResource(R.string.ph_section_amenities)) {
            AmenitiesGrid(
                listOf(
                    stringResource(R.string.ph_amenity_wifi),
                    stringResource(R.string.ph_amenity_parking),
                    stringResource(R.string.ph_amenity_kitchen),
                    stringResource(R.string.ph_amenity_tv_netflix)
                )
            )
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.ph_section_description), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.ph_description_text),
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
            Text(title, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 18.sp)
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

@Preview(name = "Host · Propiedades", showBackground = true, showSystemUi = true)
@Composable
fun PropertiesHostPreview() {
    TripWiseTheme { PropertiesHost() }
}

@Preview(name = "Host · Dark", showBackground = true, showSystemUi = true)
@Composable
fun PropertiesHostPreviewDark() {
    TripWiseTheme { PropertiesHost() }
}
