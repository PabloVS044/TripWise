package uvg.edu.tripwise.host.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uvg.edu.tripwise.data.model.PropertyReviews
import uvg.edu.tripwise.data.repository.PropertyRepository

data class ReviewsUiState(
    val loading: Boolean = false,
    val data: PropertyReviews? = null,
    val error: String? = null
)

class ReviewsViewModel(
    private val repository: PropertyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewsUiState(loading = true))
    val state: StateFlow<ReviewsUiState> = _state.asStateFlow()

    fun load(propertyId: String) {
        viewModelScope.launch {
            _state.value = ReviewsUiState(loading = true)
            try {
                val reviews = repository.getReviewsByProperty(propertyId)
                _state.value = ReviewsUiState(data = reviews)
            } catch (e: Exception) {
                _state.value = ReviewsUiState(error = e.message ?: "Error desconocido")
            }
        }
    }

    companion object {
        fun provideFactory(repository: PropertyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ReviewsViewModel::class.java)) {
                        return ReviewsViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
