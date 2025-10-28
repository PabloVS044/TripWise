package uvg.edu.tripwise.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT
import java.util.Date

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("TripWiseAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        private const val TAG = "SessionManager"
    }

    fun saveUserDetails(token: String, userId: String, email: String, role: String) {
        Log.d(TAG, "Guardando sesión: userId=$userId, email=$email, role=$role")

        // Validar que los datos no estén vacíos antes de guardar
        if (token.isBlank() || userId.isBlank() || email.isBlank() || role.isBlank()) {
            Log.e(TAG, "Error: Intentando guardar datos vacíos")
            return
        }

        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putString(USER_ID, userId)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, role)
        editor.apply()
        Log.d(TAG, "Sesión guardada exitosamente")
    }

    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        val token = fetchAuthToken()
        val userId = getUserId()
        val userRole = getUserRole()
        val userEmail = getUserEmail()

        Log.d(TAG, "=== Verificando sesión ===")
        Log.d(TAG, "Token existe: ${token != null}")
        Log.d(TAG, "UserId: $userId")
        Log.d(TAG, "UserRole: $userRole")
        Log.d(TAG, "UserEmail: $userEmail")

        // Validar que TODOS los campos necesarios existan y no estén vacíos
        if (token.isNullOrBlank() || userId.isNullOrBlank() || userRole.isNullOrBlank() || userEmail.isNullOrBlank()) {
            Log.d(TAG, "Sesión inválida: faltan datos críticos")
            // Si hay algún dato guardado pero no todos, limpiar
            if (token != null || userId != null || userRole != null || userEmail != null) {
                Log.d(TAG, "Limpiando datos parciales/corruptos")
                clearSession()
            }
            return false
        }

        // Validar el token JWT
        return try {
            val jwt = JWT(token)
            val expirationDate = jwt.expiresAt

            Log.d(TAG, "Fecha expiración token: $expirationDate")

            if (expirationDate != null && expirationDate.after(Date())) {
                Log.d(TAG, "✓ Sesión válida y activa")
                true
            } else {
                Log.d(TAG, "✗ Token expirado, limpiando sesión")
                clearSession()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error al validar token: ${e.message}", e)
            clearSession()
            false
        }
    }

    fun clearSession() {
        Log.d(TAG, "Limpiando sesión completa")
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
        Log.d(TAG, "Sesión limpiada exitosamente")
    }
}