package uvg.edu.tripwise.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Importa 'await()' para las tareas de Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // <-- ¡IMPORTANTE! (Requiere la dependencia)
import uvg.edu.tripwise.auth.SessionManager
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import java.util.UUID

// El UiState sigue igual, está perfecto
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
    // FirebaseStorage.getInstance() leerá automáticamente tu google-services.json
    private val storage = FirebaseStorage.getInstance()

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

    /**
     * Esta es la ÚNICA función que la UI debe llamar para guardar.
     * Maneja toda la lógica secuencialmente:
     * 1. Actualiza contraseña (si es necesario)
     * 2. Sube la imagen (si es necesario)
     * 3. Actualiza el perfil de usuario
     */
    fun updateProfile(
        name: String?,
        email: String, // El email no se actualiza, pero el repo lo pide
        profileImageUri: Uri?,
        interests: List<String>?,
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?
    ) {
        if (currentUserId == null) {
            _uiState.update { it.copy(errorMessage = "Error de autenticación. No se puede actualizar.") }
            return
        }

        // Inicia UNA sola corutina para todo el proceso
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isUpdateSuccess = false) }

            try {
                // --- PASO 1: Actualizar Contraseña (Lógica Mejorada) ---

                // Solo si el usuario escribió algo en "Nueva Contraseña"
                if (!newPassword.isNullOrBlank()) {

                    // 1. Validar que la "Contraseña Actual" no esté vacía
                    if (currentPassword.isNullOrBlank()) {
                        throw IllegalStateException("Debes ingresar tu contraseña actual para cambiarla.")
                    }

                    // 2. Validar que las nuevas contraseñas coincidan
                    if (newPassword != confirmPassword) {
                        throw IllegalStateException("Las nuevas contraseñas no coinciden.")
                    }

                    // 3. Intentar actualizar en el backend
                    val passwordSuccess = userRepository.updatePassword(currentUserId, currentPassword, newPassword)
                    if (!passwordSuccess) {
                        // Si el backend la rechaza, es (casi seguro) porque la contraseña actual es incorrecta
                        throw IllegalStateException("La contraseña actual es incorrecta. Verifícala.")
                    }
                }
                // --- FIN PASO 1 ---

                // --- PASO 2: Subir Imagen (si se seleccionó una nueva) ---
                var newPfpUrl: String? = uiState.value.user?.pfp // Por defecto, usa la URL antigua

                if (profileImageUri != null) {
                    _uiState.update { it.copy(isImageUploading = true) }

                    val storageRef = storage.reference
                    // Crea una referencia única para la imagen
                    val imageRef = storageRef.child("profile_images/${currentUserId}/${UUID.randomUUID()}")

                    // Sube el archivo y espera a que termine (gracias a 'await()')
                    imageRef.putFile(profileImageUri).await()

                    // Obtiene la URL de descarga y espera a que esté lista
                    newPfpUrl = imageRef.downloadUrl.await().toString()

                    _uiState.update { it.copy(isImageUploading = false) }
                }
                // Ya tenemos la URL de la imagen (nueva o la antigua)

                // --- PASO 3: Actualizar Datos del Usuario ---

                // --- ¡¡AQUÍ ESTÁ LA CORRECCIÓN!! ---
                // Añadimos el parámetro 'role', pasando el rol actual del usuario.
                val updatedUser = userRepository.updateUser(
                    id = currentUserId,
                    name = name,
                    email = email,
                    pfp = newPfpUrl, // Usa la URL que obtuvimos
                    role = uiState.value.user?.role, // <-- PARÁMETRO AÑADIDO
                    interests = interests
                )
                // --- FIN DE LA CORRECCIÓN ---

                // --- PASO 4: Éxito Total ---
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = updatedUser, // Muestra el usuario actualizado
                        isUpdateSuccess = true,
                        isImageUploading = false // Asegurarse de que esté en false
                    )
                }

            } catch (e: Exception) {
                // --- MANEJO DE ERRORES (para CUALQUIER paso) ---
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isImageUploading = false,
                        // Mostrará los mensajes de error mejorados del PASO 1
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