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

// El UiState es el mismo
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isImageUploading: Boolean = false,
    val errorMessage: String? = null,
    val isUpdateSuccess: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    // Usa tu SessionManager existente
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Usa el método de tu SessionManager
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

    /**
     * Esta función AHORA SÍ usa el 'context' para la subida a Cloudinary
     */
    fun updateProfile(
        name: String?,
        email: String,
        profileImageUri: Uri?,
        interests: List<String>?,
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?,
        context: Context // ++ PARÁMETRO AÑADIDO (Y NECESARIO) ++
    ) {
        if (currentUserId == null) {
            _uiState.update { it.copy(errorMessage = "Error de autenticación. No se puede actualizar.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isUpdateSuccess = false) }

            try {
                // --- PASO 1: Actualizar Contraseña ---
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

                // --- PASO 2: Subir Imagen (Lógica de Cloudinary) ---
                var newPfpUrl: String? = uiState.value.user?.pfp // URL antigua por defecto

                if (profileImageUri != null) {
                    _uiState.update { it.copy(isImageUploading = true) }

                    // Llama al repositorio para subir a Cloudinary
                    val response = userRepository.uploadImage(profileImageUri, context)
                    newPfpUrl = response.url // Obtiene la nueva URL de Cloudinary

                    _uiState.update { it.copy(isImageUploading = false) }
                }

                // --- PASO 3: Actualizar Datos del Usuario ---
                val updatedUser = userRepository.updateUser(
                    id = currentUserId,
                    name = name,
                    email = email,
                    pfp = newPfpUrl, // Pasa la URL (nueva o antigua)
                    role = uiState.value.user?.role, // Pasa el rol actual
                    interests = interests
                )

                // --- PASO 4: Éxito Total ---
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = updatedUser, // Muestra el usuario actualizado
                        isUpdateSuccess = true,
                        isImageUploading = false
                    )
                }

            } catch (e: Exception) {
                // --- MANEJO DE ERRORES ---
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

    /**
     * Limpia el mensaje de error una vez que se ha mostrado (ej. en un Snackbar)
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}