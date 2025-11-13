package uvg.edu.tripwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Componente de calendario reutilizable que soporta:
 * - Modo solo visualización (como en Host)
 * - Modo selección de rango de fechas (como en Reservation)
 */
@Composable
fun SelectableCalendar(
    unavailableDates: Set<String>, // Set de fechas no disponibles en formato "YYYY-MM-DD"
    isLoading: Boolean = false,
    selectionMode: Boolean = true, // true = permite seleccionar, false = solo visualización
    onDateRangeSelected: ((startDate: String, endDate: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var monthZero by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    
    // Estado de selección
    var selectedStart by remember { mutableStateOf<String?>(null) }
    var selectedEnd by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        // Header con navegación de mes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                if (monthZero == 0) {
                    monthZero = 11
                    year -= 1
                } else {
                    monthZero -= 1
                }
            }) {
                Text("◀", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = getMonthTitle(year, monthZero),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = {
                if (monthZero == 11) {
                    monthZero = 0
                    year += 1
                } else {
                    monthZero += 1
                }
            }) {
                Text("▶", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E40AF)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Leyenda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF1E40AF), text = "Disponible")
            LegendItem(color = Color(0xFFFFE5E5), text = "Ocupado")
            if (selectionMode) {
                LegendItem(color = Color(0xFFE3F2FD), text = "Seleccionado")
            }
        }

        Spacer(Modifier.height(16.dp))

        MonthGrid(
            year = year,
            monthZero = monthZero,
            unavailableDates = unavailableDates,
            selectedStart = selectedStart,
            selectedEnd = selectedEnd,
            selectionMode = selectionMode,
            onDateClick = { dateKey ->
                if (!selectionMode) return@MonthGrid

                if (selectedStart == null) {
                    // Primera selección
                    selectedStart = dateKey
                    selectedEnd = null
                } else if (selectedEnd == null) {
                    // Segunda selección
                    val start = selectedStart!!
                    if (dateKey < start) {
                        selectedStart = dateKey
                        selectedEnd = start
                    } else {
                        selectedEnd = dateKey
                    }
                    
                    // Validar que no haya fechas no disponibles en el rango
                    if (isRangeValid(selectedStart!!, selectedEnd!!, unavailableDates)) {
                        onDateRangeSelected?.invoke(selectedStart!!, selectedEnd!!)
                    } else {
                        // Reiniciar selección si el rango incluye fechas no disponibles
                        selectedStart = null
                        selectedEnd = null
                    }
                } else {
                    // Reiniciar selección
                    selectedStart = dateKey
                    selectedEnd = null
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    monthZero: Int,
    unavailableDates: Set<String>,
    selectedStart: String?,
    selectedEnd: String?,
    selectionMode: Boolean,
    onDateClick: (String) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZero)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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

    // Encabezado de días de la semana
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
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    // Filas del calendario
    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { dayOrNull ->
                    val dateKey = dayOrNull?.let { formatDateKey(year, monthZero, it) }
                    val isUnavailable = dateKey?.let { unavailableDates.contains(it) } ?: false
                    val isSelected = dateKey != null && (dateKey == selectedStart || dateKey == selectedEnd)
                    val isInRange = dateKey != null && selectedStart != null && selectedEnd != null &&
                            dateKey >= selectedStart && dateKey <= selectedEnd

                    DayCell(
                        day = dayOrNull,
                        isUnavailable = isUnavailable,
                        isSelected = isSelected,
                        isInRange = isInRange,
                        isPast = dateKey?.let { isPastDate(it) } ?: false,
                        selectionMode = selectionMode,
                        onClick = {
                            if (dateKey != null && !isUnavailable && selectionMode && !isPastDate(dateKey)) {
                                onDateClick(dateKey)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: Int?,
    isUnavailable: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    isPast: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit
) {
    val base = Modifier
        .weight(1f)
        .aspectRatio(1f)

    if (day == null) {
        Box(base)
        return
    }

    val bg = when {
        isSelected -> Color(0xFF1E40AF)
        isInRange -> Color(0xFFE3F2FD)
        isUnavailable -> Color(0xFFFFE5E5)
        isPast -> Color(0xFFF5F5F5)
        else -> Color.Transparent
    }

    val fg = when {
        isSelected -> Color.White
        isUnavailable -> Color(0xFFD32F2F)
        isPast -> Color.LightGray
        else -> MaterialTheme.colorScheme.onSurface
    }

    val clickable = if (selectionMode && !isUnavailable && !isPast) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = base.then(clickable),
        shape = if (isSelected) CircleShape else RoundedCornerShape(8.dp),
        tonalElevation = if (isUnavailable) 1.dp else 0.dp,
        color = bg,
        border = if (isInRange && !isSelected) BorderStroke(1.dp, Color(0xFF1E40AF)) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = day.toString(),
                color = fg,
                fontWeight = if (isSelected || isUnavailable) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Funciones auxiliares
private fun getMonthTitle(year: Int, monthZero: Int): String {
    val months = DateFormatSymbols(Locale.getDefault()).months
    val name = months[monthZero].replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
    return "$name $year"
}

private fun formatDateKey(year: Int, monthZero: Int, day: Int): String =
    String.format("%04d-%02d-%02d", year, monthZero + 1, day)

private fun isPastDate(dateKey: String): Boolean {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(dateKey)
    return date?.before(today.time) ?: false
}

private fun isRangeValid(startDate: String, endDate: String, unavailableDates: Set<String>): Boolean {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val start = dateFormat.parse(startDate) ?: return false
    val end = dateFormat.parse(endDate) ?: return false

    val cal = Calendar.getInstance()
    cal.time = start

    while (!cal.time.after(end)) {
        val currentDateKey = dateFormat.format(cal.time)
        if (unavailableDates.contains(currentDateKey)) {
            return false
        }
        cal.add(Calendar.DATE, 1)
    }

    return true
}
