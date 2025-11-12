package uvg.edu.tripwise.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uvg.edu.tripwise.data.repository.PropertyRepository
import uvg.edu.tripwise.host.reviews.ReviewsViewModel

class ReviewsViewModelFactory(
    private val repository: PropertyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewsViewModel::class.java)) {
            return ReviewsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
