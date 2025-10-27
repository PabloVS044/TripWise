package uvg.edu.tripwise.host

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

/* ====== Colors (aligned with Sign In button #2563EB) ====== */
private val PrimaryBlue = Color(0xFF2563EB) // RGB(37,99,235)
private val SuccessGreen = Color(0xFF0AA12E)
private val DangerRed   = Color(0xFFE2265B)
private val PageBg      = Color(0xFFF7F7FB)

/* ====== Tabs ====== */
enum class HostTab(@StringRes val titleRes: Int) {
    Overview(R.string.tab_overview),
    Bookings(R.string.tab_bookings),
    Reviews(R.string.tab_reviews),
    Calendar(R.string.tab_calendar)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesHost(
    modifier: Modifier = Modifier,
    propertyId: String? = null,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { PropertyRepository() }

    var selectedTab by remember { mutableStateOf(HostTab.Overview) }
    var showSearchSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Remote state: properties of the logged-in owner
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var loadingProps by remember { mutableStateOf(false) }
    var errorProps by remember { mutableStateOf<String?>(null) }

    // Selection (list → detail)
    var selectedProperty by remember { mutableStateOf<Property?>(null) }

    // Initial load using session user
    LaunchedEffect(Unit) {
        val prefs  = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", prefs.getString("user_id", null))
        if (userId.isNullOrBlank()) {
            errorProps = context.getString(R.string.error_no_user_session)
            return@LaunchedEffect
        }
        try {
            loadingProps = true
            val list = repo.getPropertiesByOwner(userId)
            properties = list
            // If a propertyId came in, preselect it
            propertyId?.let { id -> list.firstOrNull { it.id == id }?.let { selectedProperty = it } }
        } catch (e: Exception) {
            errorProps = e.message
        } finally {
            loadingProps = false
        }
    }

    // When switching tabs, close the search sheet (if not in Bookings)
    LaunchedEffect(selectedTab) {
        if (selectedTab != HostTab.Bookings && showSearchSheet) showSearchSheet = false
    }

    Scaffold(
        topBar = {
            Box {
                LogoAppTopBar(onLogout)

                // Host profile (with session USER_ID)
                IconButton(
                    onClick = {
                        val prefs  = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                        val userId = prefs.getString("USER_ID", prefs.getString("user_id", null))
                        val intent = Intent(context, ProfileHostActivity::class.java)
                            .putExtra(EXTRA_USER_ID, userId)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(start = 8.dp, top = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.cd_go_to_profile),
                        tint = PrimaryBlue
                    )
                }
            }
        }
    ) { inner ->
        Box(
            modifier = modifier
                .padding(inner)
                .fillMaxSize()
                .background(PageBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                /* KPIs */
                StatsGrid()
                Spacer(Modifier.height(16.dp))

                /* Divider */
                Divider(thickness = 1.dp, color = PrimaryBlue.copy(alpha = 0.15f))
                Spacer(Modifier.height(16.dp))

                if (selectedProperty == null) {
                    // ===== PROPERTY LIST =====
                    when {
                        loadingProps -> PlaceholderSection(stringResource(R.string.loading_properties))
                        errorProps != null -> PlaceholderSection(stringResource(R.string.error_with_message, errorProps ?: ""))
                        properties.isEmpty() -> PlaceholderSection(stringResource(R.string.no_properties_yet))
                        else -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.your_properties_header),
                                    color = PrimaryBlue,
                                    fontSize = 22.sp, // larger
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(
                                    onClick = {
                                        val i = Intent(context, CreatePropertyActivity::class.java)
                                        context.startActivity(i)
                                    },
                                    shape = RoundedCornerShape(14.dp), // soft pill
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = Color.White
                                    )
                                ) { Text(stringResource(R.string.btn_new_property)) }
                            }
                            Spacer(Modifier.height(12.dp))
                            properties.forEach { prop ->
                                PropertyRowCard(
                                    name = prop.name,
                                    onClick = {
                                        selectedProperty = prop
                                        selectedTab = HostTab.Overview
                                    }
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                } else {
                    // ===== DETAIL: NAVBAR (Overview/Bookings/Reviews/Calendar) =====
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { selectedProperty = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = PrimaryBlue
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = selectedProperty!!.name,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HostTopTabBar(selected = selectedTab, onSelected = { selectedTab = it })
                    Spacer(Modifier.height(24.dp))

                    when (selectedTab) {
                        HostTab.Overview  -> SummarySectionRemote(propertyId = selectedProperty!!.id)
                        HostTab.Bookings  -> ReservationsSection()
                        HostTab.Reviews   -> PlaceholderSection(stringResource(R.string.reviews_coming_soon))
                        HostTab.Calendar  -> PlaceholderSection(stringResource(R.string.calendar_coming_soon))
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

            // Filters FAB only on Bookings tab in detail
            if (selectedProperty != null && selectedTab == HostTab.Bookings) {
                ExtendedFloatingActionButton(
                    onClick = { showSearchSheet = true },
                    icon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.cd_search)) },
                    text = { Text(stringResource(R.string.filter_search)) },
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

/* ---------------- LIST: simple card with only the name ---------------- */
@Composable
private fun PropertyRowCard(name: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = name,
            color = Color(0xFF102A43),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold, // bold names
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

/* ---------------- Overview (remote; uses repo/back) ---------------- */
@Composable
private fun SummarySectionRemote(
    propertyId: String?,
    repo: PropertyRepository = PropertyRepository()
) {
    var property by remember { mutableStateOf<Property?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(propertyId) {
        val id = propertyId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        try {
            loading = true
            property = repo.getPropertyById(id)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    when {
        propertyId.isNullOrBlank() -> PlaceholderSection(stringResource(R.string.select_property_to_see_overview))
        loading -> PlaceholderSection(stringResource(R.string.loading_property))
        error != null -> PlaceholderSection(stringResource(R.string.error_with_message, error ?: ""))
        property == null -> PlaceholderSection(stringResource(R.string.property_not_found))
        else -> SummarySectionBound(property = property!!)
    }
}

@Composable
private fun SummarySectionBound(property: Property) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhiteCard(title = stringResource(R.string.basic_information)) {
            KeyValueRow(stringResource(R.string.label_type),        property.propertyType.ifBlank { stringResource(R.string.placeholder_em_dash) })
            KeyValueRow(stringResource(R.string.label_guests),      property.capacity.toString())
            KeyValueRow(stringResource(R.string.label_rooms),       stringResource(R.string.placeholder_em_dash)) // not available in back
            KeyValueRow(stringResource(R.string.label_bathrooms),   stringResource(R.string.placeholder_em_dash)) // not available in back
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_price_per_night), fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
                Text("$${property.pricePerNight}", fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
        }

        WhiteCard(title = stringResource(R.string.amenities_title)) {
            val amenities = property.amenities
            if (amenities.isEmpty()) Text(stringResource(R.string.placeholder_em_dash), color = Color(0xFF26364D)) else AmenitiesGrid(amenities)
        }

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.description_title), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(property.description.ifBlank { stringResource(R.string.placeholder_em_dash) }, color = Color(0xFF102A43), lineHeight = 20.sp)
            }
        }
    }
}

/* ---------------- Filter bottom sheet ---------------- */
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
        Text(stringResource(R.string.filter_bookings_title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.search_by_guest_notes)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.status_title), fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = confirmed, onClick = { confirmed = !confirmed }, label = { Text(stringResource(R.string.status_confirmed)) })
            FilterChip(selected = pending, onClick = { pending = !pending }, label = { Text(stringResource(R.string.status_pending)) })
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White)
            ) { Text(stringResource(R.string.action_apply)) }
        }
        Spacer(Modifier.height(12.dp))
    }
}

/* ---------------- Supporting UI ---------------- */
@Composable
private fun StatsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = stringResource(R.string.stat_occupancy), value = "85%", bg = Color(0xFF2E63F1), modifier = Modifier.weight(1f))
            StatCard(title = stringResource(R.string.stat_revenue_per_month), value = "$4500", bg = SuccessGreen, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = stringResource(R.string.stat_rating), value = "4.8", bg = Color(0xFF8E198A), modifier = Modifier.weight(1f))
            StatCard(title = stringResource(R.string.stat_response_rate), value = "98%", bg = DangerRed, modifier = Modifier.weight(1f))
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

    Surface(modifier = modifier.fillMaxWidth(), color = PrimaryBlue, shape = RoundedCornerShape(8.dp)) {
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

/* ============================ BOOKINGS (demo UI) ============================ */
private enum class ReservationStatus { Confirmed, Pending }
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
        Reservation("Ana Gomez", "Oct 12–15", "$320", ReservationStatus.Confirmed),
        Reservation("Luis Perez", "Oct 20–22", "$210", ReservationStatus.Pending),
        Reservation("Carla Soto", "Nov 02–05", "$450", ReservationStatus.Confirmed)
    )
    val activeCount = reservations.count { it.status == ReservationStatus.Confirmed }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.your_bookings_title), color = PrimaryBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            PillTag(text = stringResource(R.string.active_count, activeCount), color = SuccessGreen)
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
    Surface(color = color, contentColor = Color.White, shape = RoundedCornerShape(50), shadowElevation = 0.dp) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReservationItem(
    reservation: Reservation,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(2.dp, PrimaryBlue) else BorderStroke(1.dp, Color(0xFFE8E8F0))
    val statusColor = if (reservation.status == ReservationStatus.Confirmed) SuccessGreen else DangerRed
    val statusText = if (reservation.status == ReservationStatus.Confirmed) stringResource(R.string.status_confirmed) else stringResource(R.string.status_pending)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border,
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(reservation.guestName, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF102A43))
                Spacer(Modifier.height(4.dp))
                Text(reservation.dateRange, color = Color(0xFF6B7A90))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(reservation.amountUsd, color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatusChip(text = statusText, color = statusColor)
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(color = color, contentColor = Color.White, shape = RoundedCornerShape(50), shadowElevation = 0.dp) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.SemiBold)
    }
}

/* ---------------- Shared helpers ---------------- */
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

/* ---------------- Preview ---------------- */
@Preview(name = "Host · Properties (list first)", showBackground = true, showSystemUi = true)
@Composable
fun PropertiesHostPreview() {
    TripWiseTheme { PropertiesHost(propertyId = null) }
}
