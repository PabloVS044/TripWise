package uvg.edu.tripwise.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.repository.PropertyRepository

class PropertyViewModel : ViewModel() {
    private val repository = PropertyRepository()

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty.asStateFlow()

    init {
        loadProperties()
    }

    fun loadProperties() {
        viewModelScope.launch {
            try {
                _properties.value = repository.getProperties()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun getPropertyById(id: String) {
        viewModelScope.launch {
            try {
                val property = _properties.value.find { it.id == id }
                _selectedProperty.value = property
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun clearSelectedProperty() {
        _selectedProperty.value = null
    }
}