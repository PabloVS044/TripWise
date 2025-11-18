package uvg.edu.tripwise.viewModel

import android.app.Application
import android.content.Context // ++ IMPORTACIÓN AÑADIDA ++
import android.net.Uri
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


data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isImageUploading: Boolean = false,
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


    fun updateProfile(
        name: String?,
        email: String,
        profileImageUri: Uri?,
        interests: List<String>?,
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?,
        context: Context
    ) {
        if (currentUserId == null) {
            _uiState.update { it.copy(errorMessage = "Error de autenticación. No se puede actualizar.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isUpdateSuccess = false) }

            try {

                if (!newPassword.isNullOrBlank()) {
                    if (currentPassword.isNullOrBlank()) {
                        throw IllegalStateException("Debes ingresar tu contraseña actual para cambiarla.")
                    }
                    if (newPassword != confirmPassword) {
                        throw IllegalStateException("Las nuevas contraseñas no coinciden.")
                    }
                    val passwordSuccess = userRepository.updatePassword(currentUserId, currentPassword, newPassword)
                    if (!passwordSuccess) {
                        throw IllegalStateException("La contraseña actual es incorrecta. Verifícala.")
                    }
                }


                var newPfpUrl: String? = uiState.value.user?.pfp

                if (profileImageUri != null) {
                    _uiState.update { it.copy(isImageUploading = true) }


                    val response = userRepository.uploadImage(profileImageUri, context)
                    newPfpUrl = response.url

                    _uiState.update { it.copy(isImageUploading = false) }
                }


                val updatedUser = userRepository.updateUser(
                    id = currentUserId,
                    name = name,
                    email = email,
                    pfp = newPfpUrl,
                    role = uiState.value.user?.role,
                    interests = interests
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = updatedUser,
                        isUpdateSuccess = true,
                        isImageUploading = false
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isImageUploading = false,
                        errorMessage = e.message ?: "Ocurrió un error desconocido"
                    )
                }
            }
        }
    }


    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}