package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UpdateUserRequest

class UserRepository {
    private val api = RetrofitInstance.api

    suspend fun getUsers(): List<User> {
        val response = api.getUsers()
        return response.map { apiUser ->
            User(
                id = apiUser.id,
                name = apiUser.name,
                email = apiUser.email,
                deleted = apiUser.deleted?.let { deleted ->
                    Deleted(
                        isDeleted = deleted.isDeleted,
                        at = deleted.at
                    )
                }
            )
        }
    }

    suspend fun getUserById(id: String): User {
        val apiUser = api.getUserById(id)
        return User(
            id = apiUser.id,
            name = apiUser.name,
            email = apiUser.email,
            deleted = apiUser.deleted?.let { deleted ->
                Deleted(
                    isDeleted = deleted.isDeleted,
                    at = deleted.at
                )
            }
        )
    }

    suspend fun updateUser(id: String, name: String, email: String): User {
        val request = UpdateUserRequest(name = name, email = email)
        val updatedApiUser = api.updateUser(id, request)
        return User(
            id = updatedApiUser.id,
            name = updatedApiUser.name,
            email = updatedApiUser.email,
            deleted = updatedApiUser.deleted?.let { deleted ->
                Deleted(
                    isDeleted = deleted.isDeleted,
                    at = deleted.at
                )
            }
        )
    }

    suspend fun softDeleteUser(id: String): Boolean {
        return try {
            api.softDeleteUser(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}