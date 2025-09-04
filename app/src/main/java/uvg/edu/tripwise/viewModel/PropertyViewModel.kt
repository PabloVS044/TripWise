package uvg.edu.tripwise.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uvg.edu.tripwise.data.repository.propertyRepository
import uvg.edu.tripwise.data.model.Post

class PropertyViewModel(
    private val repo: propertyRepository = propertyRepository()
) : ViewModel() {

    private val _properties = MutableStateFlow<List<Post>>(emptyList())
    val properties: StateFlow<List<Post>> = _properties.asStateFlow()

    private val _selectedProperty = MutableStateFlow<Post?>(null)
    val selectedProperty: StateFlow<Post?> = _selectedProperty.asStateFlow()

    init {
        loadProperties()
    }

    fun loadProperties() {
        viewModelScope.launch {
            try {
                val result = repo.getProperties()
                _properties.value = result
            } catch (e: Exception) {
                // Manejo de errores
                e.printStackTrace()
            }
        }
    }

    fun getPropertyById(id: String) {
        viewModelScope.launch {
            try {
                val property = repo.getPropertyById(id)
                _selectedProperty.value = property
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSelectedProperty() {
        _selectedProperty.value = null
    }
}
