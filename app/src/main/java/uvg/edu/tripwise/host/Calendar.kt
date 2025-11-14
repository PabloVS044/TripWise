package uvg.edu.tripwise.host

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uvg.edu.tripwise.data.repository.AvailabilityRepository
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarSection(
    propertyId: String,
    reloadKey: Int, // ✅ para recarga vía pull-to-refresh global
    modifier: Modifier = Modifier,
    vm: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(AvailabilityRepository())
    )
) {
    // recarga al entrar / cambiar propiedad / refrescar manual
    LaunchedEffect(propertyId, reloadKey) { vm.loadAvailability(propertyId) }

    val state by vm.state.collectAsState()

    // Estado del mes actual sin java.time
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var monthZero by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    Column(modifier = modifier.padding(16.dp)) {

        // Header con navegación de mes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = {
                if (monthZero == 0) { monthZero = 11; year -= 1 } else { monthZero -= 1 }
            }) { Text("◀") }

            Text(
                text = monthTitle(year, monthZero),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = {
                if (monthZero == 11) { monthZero = 0; year += 1 } else { monthZero += 1 }
            }) { Text("▶") }
        }

        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            state.error != null -> Text(
                text = state.error ?: "Error",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        MonthGrid(
            year = year,
            monthZero = monthZero,
            unavailable = state.unavailable
        )
    }
}

private fun monthTitle(year: Int, monthZero: Int): String {
    val months = DateFormatSymbols(Locale.getDefault()).months
    val name = months[monthZero].replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    return "$name $year"
}

@Composable
private fun MonthGrid(
    year: Int,
    monthZero: Int,
    unavailable: Set<String> // "YYYY-MM-DD"
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZero)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Dom ... 7=Sáb
    val firstDayOfWeek = Calendar.MONDAY
    val leadingEmpty = ((dayOfWeek - firstDayOfWeek + 7) % 7)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells: List<Int?> = buildList {
        repeat(leadingEmpty) { add(null) }
        for (d in 1..daysInMonth) add(d)
        while (size % 7 != 0) add(null)
    }

    // Encabezado
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val order = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val short = DateFormatSymbols(Locale.getDefault()).shortWeekdays
        for (wd in order) {
            Text(
                text = short[wd],
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    // ⛔️ Sin LazyColumn (scroll lo hace el padre)
    val rows = remember(cells) { cells.chunked(7) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { dayOrNull ->
                    DayCell(
                        year = year,
                        monthZero = monthZero,
                        day = dayOrNull,
                        isUnavailable = dayOrNull?.let { d ->
                            val key = dateKey(year, monthZero, d)
                            unavailable.contains(key)
                        } ?: false
                    )
                }
            }
        }
    }
}

private fun dateKey(year: Int, monthZero: Int, day: Int): String =
    String.format("%04d-%02d-%02d", year, monthZero + 1, day)

@Composable
private fun RowScope.DayCell(
    year: Int,
    monthZero: Int,
    day: Int?,
    isUnavailable: Boolean
) {
    val base = Modifier
        .weight(1f)
        .aspectRatio(1f)

    if (day == null) {
        Box(base)
        return
    }

    val bg = if (isUnavailable) Color(0xFFFFE5E5) else Color.Transparent
    val fg = if (isUnavailable) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = base,
        shape = RoundedCornerShape(10.dp),
        tonalElevation = if (isUnavailable) 1.dp else 0.dp,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "%02d".format(day),
                color = fg,
                fontWeight = if (isUnavailable) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
