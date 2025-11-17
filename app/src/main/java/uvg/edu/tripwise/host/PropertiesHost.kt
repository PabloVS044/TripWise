package uvg.edu.tripwise.host

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.HostStatsRepository
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.host.reviews.ReviewsSection
import uvg.edu.tripwise.host.reviews.ReviewsViewModel
import uvg.edu.tripwise.host.ReviewsViewModelFactory
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import java.text.NumberFormat
import java.util.Locale

private val PrimaryBlue = Color(0xFF2563EB)
private val SuccessGreen = Color(0xFF0AA12E)
private val DangerRed = Color(0xFFE2265B)
private val PageBg = Color(0xFFF7F7FB)
private val SelectedBg = Color(0xFFEFF4FF)

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
    val scope = rememberCoroutineScope()
    val propRepo = remember { PropertyRepository() }
    val statsRepo = remember { HostStatsRepository() }
    var selectedTab by remember { mutableStateOf(HostTab.Overview) }
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var loadingProps by remember { mutableStateOf(false) }
    var errorProps by remember { mutableStateOf<String?>(null) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var detailReloadTick by remember { mutableStateOf(0) }
    var stats by remember { mutableStateOf<HostStatsRepository.HostStatsUi?>(null) }
    var statsLoading by remember { mutableStateOf(false) }
    var statsError by remember { mutableStateOf<String?>(null) }

    val reviewsViewModel: ReviewsViewModel = viewModel(
        factory = ReviewsViewModelFactory(propRepo)
    )

    fun loadOwnerStats() {
        scope.launch {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("USER_ID", prefs.getString("user_id", null))
            if (userId.isNullOrBlank()) {
                statsError = context.getString(R.string.error_no_user_session)
                return@launch
            }
            try {
                statsLoading = true
                stats = statsRepo.getStatsUi(userId)
                statsError = null
            } catch (e: Exception) {
                statsError = e.message
            } finally {
                statsLoading = false
            }
        }
    }

    fun loadPropertiesForSession() {
        scope.launch {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("USER_ID", prefs.getString("user_id", null))
            if (userId.isNullOrBlank()) {
                errorProps = context.getString(R.string.error_no_user_session)
                isRefreshing = false
                return@launch
            }
            try {
                loadingProps = true
                val list = propRepo.getPropertiesByOwner(userId)
                properties = list
                propertyId?.let { id ->
                    list.firstOrNull { it.id == id }?.let { selectedProperty = it }
                }
                loadOwnerStats()
            } catch (e: Exception) {
                errorProps = e.message
            } finally {
                loadingProps = false
                isRefreshing = false
            }
        }
    }

    fun refreshHost() {
        isRefreshing = true
        loadOwnerStats()
        if (selectedProperty == null) {
            loadPropertiesForSession()
        } else {
            detailReloadTick++
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { loadPropertiesForSession() }

    Scaffold(
        topBar = {
            Box {
                LogoAppTopBar(onLogout)
                IconButton(
                    onClick = {
                        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
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
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { refreshHost() }
            ) {
                val parentModifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())

                Column(modifier = parentModifier) {
                    StatsGrid(stats = stats, loading = statsLoading, error = statsError)

                    Spacer(Modifier.height(16.dp))
                    Divider(thickness = 1.dp, color = PrimaryBlue.copy(alpha = 0.15f))
                    Spacer(Modifier.height(16.dp))

                    if (selectedProperty == null) {
                        when {
                            loadingProps -> PlaceholderSection(stringResource(R.string.loading_properties))
                            errorProps != null -> PlaceholderSection(
                                stringResource(
                                    R.string.error_with_message,
                                    errorProps ?: ""
                                )
                            )

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
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Button(
                                        onClick = {
                                            val i = Intent(
                                                context,
                                                CreatePropertyActivity::class.java
                                            )
                                            context.startActivity(i)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryBlue,
                                            contentColor = Color.White
                                        )
                                    ) { Text(stringResource(R.string.btn_new_property)) }
                                }
                                Spacer(Modifier.height(12.dp))
                                properties.forEach { prop ->
                                    val rowHeight = 48.dp

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PropertyRowCard(
                                            property = prop,
                                            onClick = {
                                                selectedProperty = prop
                                                selectedTab = HostTab.Overview
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(rowHeight),
                                            shape = RoundedCornerShape(
                                                topStart = 10.dp,
                                                bottomStart = 10.dp,
                                                topEnd = 0.dp,
                                                bottomEnd = 0.dp
                                            ),
                                        )
                                        Button(
                                            onClick = {
                                                val intent = Intent(
                                                    context,
                                                    EditPropertyActivity::class.java
                                                ).apply {
                                                    putExtra(
                                                        EditPropertyActivity.EXTRA_PROPERTY_ID,
                                                        prop.id
                                                    )
                                                }
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 0.dp,
                                                bottomStart = 0.dp,
                                                topEnd = 10.dp,
                                                bottomEnd = 10.dp
                                            ),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PrimaryBlue,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 0.dp
                                            ),
                                            modifier = Modifier.height(rowHeight)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .padding(end = 6.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.action_edit),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                }

                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    } else {
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
                            HostTab.Overview -> SummarySectionRemote(
                                propertyId = selectedProperty!!.id,
                                reloadKey = detailReloadTick
                            )

                            HostTab.Bookings -> ReservationsSection(
                                propertyId = selectedProperty!!.id,
                                reloadKey = detailReloadTick
                            )

                            HostTab.Reviews -> ReviewsSection(
                                propertyId = selectedProperty!!.id,
                                viewModel = reviewsViewModel,
                                reloadKey = detailReloadTick
                            )

                            HostTab.Calendar -> CalendarSection(
                                propertyId = selectedProperty!!.id,
                                reloadKey = detailReloadTick
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyRowCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val isActive = pressed

    val borderColor by animateColorAsState(
        targetValue = if (isActive) PrimaryBlue else Color(0xFFE8E8F0),
        label = "propRowBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) SelectedBg else Color.White,
        label = "propRowBg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) PrimaryBlue else Color(0xFF94A3B8),
        label = "propRowIconTint"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isActive) PrimaryBlue else Color(0xFF102A43),
        label = "propRowTitleColor"
    )

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.clickable(
            interactionSource = interaction,
            indication = LocalIndication.current,
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon: ImageVector = iconForType(property.propertyType)
            Surface(shape = CircleShape, color = Color(0xFFF1F5F9)) {
                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = property.name,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun iconForType(type: String?): ImageVector = when (type?.lowercase()) {
    "house" -> Icons.Filled.Home
    "apartment" -> Icons.Filled.Apartment
    "cottage", "cabin" -> Icons.Filled.HolidayVillage
    "hotel" -> Icons.Filled.Hotel
    else -> Icons.Filled.Apartment
}

@Composable
private fun StatsGrid(
    stats: HostStatsRepository.HostStatsUi?,
    loading: Boolean,
    error: String?
) {
    val moneyFmt = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }
    }

    val dash = stringResource(R.string.placeholder_em_dash)

    val occ = when {
        loading || stats == null -> dash
        else -> "${stats.occupancyPct}%"
    }
    val revenue = when {
        loading || stats == null -> dash
        else -> moneyFmt.format(stats.revenueMonth)
    }
    val rating = when {
        loading || stats == null -> dash
        else -> String.format(Locale.US, "%.1f", stats.rating)
    }
    val response = when {
        loading || stats == null -> dash
        else -> "${stats.responseRatePct}%"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_occupancy),
                value = occ,
                bg = Color(0xFF2E63F1),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_revenue_per_month),
                value = revenue,
                bg = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_rating),
                value = rating,
                bg = Color(0xFF8E198A),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_response_rate),
                value = response,
                bg = DangerRed,
                modifier = Modifier.weight(1f)
            )
        }
        if (!error.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.error_with_message, error),
                color = DangerRed.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatCard(
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
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
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
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(mid).forEach { Text("• $it", color = Color(0xFF26364D)) }
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
        Box(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color(0xFF50607A), textAlign = TextAlign.Center)
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

@Preview(name = "Host · Properties", showBackground = true, showSystemUi = true)
@Composable
fun PropertiesHostPreview() {
    TripWiseTheme { PropertiesHost(propertyId = null) }
}
