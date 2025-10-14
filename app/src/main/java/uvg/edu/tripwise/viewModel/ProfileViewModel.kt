package uvg.edu.tripwise.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uvg.edu.tripwise.auth.SessionManager
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository

// Estado para la UI
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUpdateSuccess: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val currentUserId: String? = sessionManager.getUserId()

    init {
        fetchUser()
    }

    fun fetchUser() {
        if (currentUserId == null) {
            _uiState.update { it.copy(errorMessage = "No se ha podido identificar al usuario.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getUserById(currentUserId)
                _uiState.update {
                    it.copy(isLoading = false, user = user)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al cargar el perfil: ${e.message}")
                }
            }
        }
    }

    fun updateUser(name: String, email: String) {
        if (currentUserId == null) {
            _uiState.update { it.copy(errorMessage = "Error de autenticación. No se puede actualizar.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isUpdateSuccess = false) }
            try {
                val updatedUser = userRepository.updateUser(currentUserId, name, email)
                _uiState.update {
                    it.copy(isLoading = false, user = updatedUser, isUpdateSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al actualizar: ${e.message}")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}