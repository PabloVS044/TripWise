package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.network.UserApiService
import uvg.edu.tripwise.network.RetrofitInstance
import androidx.compose.ui.graphics.Color

class UserRepository(
    private val api: UserApiService = RetrofitInstance.api
) {
    suspend fun getUsers(): List<User> {
        val response = api.getUsers() // Asume que la API devuelve una lista de usuarios
        return response.map { apiUser ->
            User(
                id = apiUser.id,
                name = apiUser.name,
                email = apiUser.email,
                initial = apiUser.name.firstOrNull()?.uppercase() ?: "U",
                isActive = apiUser.deleted?.isDeleted?.not() ?: true, // Invierte isDeleted para isActive
                avatarColor = getAvatarColor(apiUser.name)
            )
        }
    }

    suspend fun softDeleteUser(id: String): Boolean {
        return try {
            api.softDeleteUser(id) // Asume que la API tiene un endpoint para desactivar
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getAvatarColor(name: String): Color {
        val colors = listOf(
            Color(0xFF8B5CF6), // Purple
            Color(0xFF06B6D4), // Cyan
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFFEF4444), // Red
            Color(0xFF3B82F6)  // Blue
        )
        return colors[name.hashCode() % colors.size]
    }
}