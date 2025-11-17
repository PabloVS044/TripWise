package uvg.edu.tripwise.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uvg.edu.tripwise.data.repository.AvailabilityRepository

data class CalendarUiState(
    val loading: Boolean = false,
    val unavailable: Set<String> = emptySet(),
    val error: String? = null
)

class CalendarViewModel(
    private val repo: AvailabilityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state

    fun loadAvailability(propertyId: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repo.getUnavailableDates(propertyId) }
                .onSuccess { dates -> _state.value = CalendarUiState(false, dates, null) }
                .onFailure { e -> _state.value = CalendarUiState(false, emptySet(), e.message ?: "Error") }
        }
    }
}
