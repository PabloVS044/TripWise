package uvg.edu.tripwise.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uvg.edu.tripwise.data.repository.AvailabilityRepository

class CalendarViewModelFactory(
    private val repo: AvailabilityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(repo) as T
    }
}
