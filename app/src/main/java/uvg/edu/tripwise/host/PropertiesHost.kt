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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

/* Colores locales */
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
fun PropertiesHost(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HostTab.Resumen) }

    var showSearchSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LaunchedEffect(selectedTab) {
        if (selectedTab != HostTab.Reservas && showSearchSheet) showSearchSheet = false
    }

    Scaffold(topBar = { LogoAppTopBar(onLogout) }) { inner ->
        Box(
            modifier = modifier
                .padding(inner)
                .fillMaxSize()
                .background(PageBg)
        ) {
            // CONTENIDO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                /* KPIs 2×2 (sin íconos) */
                StatsGrid()
                Spacer(Modifier.height(16.dp))

                HostTopTabBar(selected = selectedTab, onSelected = { selectedTab = it })
                Spacer(Modifier.height(24.dp)) // más aire bajo el menú

                when (selectedTab) {
                    HostTab.Resumen    -> SummarySection()
                    HostTab.Reservas   -> ReservationsSection()
                    HostTab.Reseñas    -> PlaceholderSection(stringResource(R.string.ph_placeholder_reviews))
                    HostTab.Calendario -> PlaceholderSection(stringResource(R.string.ph_placeholder_calendar))
                }

                Spacer(Modifier.height(24.dp))
            }

            /* FAB + SHEET SOLO EN RESERVAS */
            if (selectedTab == HostTab.Reservas) {
                ExtendedFloatingActionButton(
                    onClick = { showSearchSheet = true },
                    icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    text = { Text(stringResource(R.string.action_search_filter)) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )

                if (showSearchSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSearchSheet = false },
                        sheetState = sheetState
                    ) {
                        SearchFilterSheet(
                            onApply = { showSearchSheet = false },
                            onCancel = { showSearchSheet = false }
                        )
                    }
                }
            }
        }
    }
}

/* ---------- Sheet de búsqueda y filtros ---------- */
@Composable
private fun SearchFilterSheet(
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var confirmed by remember { mutableStateOf(true) }
    var pending by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            stringResource(R.string.search_filter_title),
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryBlue
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.filter_status), fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = confirmed,
                onClick = { confirmed = !confirmed },
                label = { Text(stringResource(R.string.ph_reservation_status_confirmed)) }
            )
            FilterChip(
                selected = pending,
                onClick = { pending = !pending },
                label = { Text(stringResource(R.string.ph_reservation_status_pending)) }
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onApply) { Text(stringResource(R.string.action_apply)) }
        }
        Spacer(Modifier.height(12.dp))
    }
}

/* ---------- Soporte UI ---------- */

@Composable
private fun StatsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.ph_kpi_occupancy),
                value = stringResource(R.string.ph_kpi_occupancy_value),
                bg = Color(0xFF2E63F1),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.ph_kpi_income_month),
                value = stringResource(R.string.ph_kpi_income_value),
                bg = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.ph_kpi_rating),
                value = stringResource(R.string.ph_kpi_rating_value),
                bg = Color(0xFF8E198A),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.ph_kpi_response),
                value = stringResource(R.string.ph_kpi_response_value),
                bg = DangerRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    bg: Color,
    modifier: Modifier = Modifier,
    minHeight: Dp = 104.dp
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier.heightIn(min = minHeight)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
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
        shape = RoundedCornerShape(8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
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
                    text = {
                        Text(
                            text = stringResource(tab.titleRes),
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}

/* ============================================ RESERVAS ======================================================== */

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
                    R.plurals.ph_reservations_active_count, activeCount, activeCount
                ),
                color = SuccessGreen
            )
        }

        Spacer(Modifier.height(14.dp))

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
        color = color, contentColor = Color.White,
        shape = RoundedCornerShape(50), shadowElevation = 0.dp
    ) {
        Text(
            text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border,
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    reservation.guestName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102A43)
                )
                Spacer(Modifier.height(4.dp))
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
        color = color, contentColor = Color.White,
        shape = RoundedCornerShape(50), shadowElevation = 0.dp
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ============================================ RESUMEN ======================================================== */

@Composable
fun SummarySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhiteCard(title = stringResource(R.string.ph_section_basic_info)) {
            KeyValueRow(stringResource(R.string.ph_label_type), stringResource(R.string.ph_summary_type_value))
            KeyValueRow(stringResource(R.string.ph_label_guests), stringResource(R.string.ph_summary_guests_value))
            KeyValueRow(stringResource(R.string.ph_label_rooms), stringResource(R.string.ph_summary_rooms_value))
            KeyValueRow(stringResource(R.string.ph_label_bathrooms), stringResource(R.string.ph_summary_bathrooms_value))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.ph_label_price_per_night), fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
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
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.ph_section_description), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.ph_description_text), color = Color(0xFF102A43), lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun WhiteCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = Color(0xFF50607A), textAlign = TextAlign.Center)
        }
    }
}

/* ---------- Previews ---------- */

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
