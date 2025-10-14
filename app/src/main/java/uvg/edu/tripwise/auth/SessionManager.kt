package uvg.edu.tripwise.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("TripWiseAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
    }

    /**
     * Función para guardar los detalles del usuario después del login.
     */
    fun saveUserDetails(token: String, userId: String, email: String, role: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putString(USER_ID, userId)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    /**
     * Función para obtener el ID del usuario guardado.
     */
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    /**
     * Función para obtener el token de autenticación.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Función para limpiar la sesión (logout).
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}