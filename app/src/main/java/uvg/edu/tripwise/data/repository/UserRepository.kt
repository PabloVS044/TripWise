package uvg.edu.tripwise.data.repository


import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UpdateUserRequest


class UserRepository {
    private val api = RetrofitInstance.api


    suspend fun getUsers(): List<User> =
        api.getUsers().map { apiUser ->
            User(
                id = apiUser.id,
                name = apiUser.name,
                email = apiUser.email,
                deleted = apiUser.deleted?.let { d ->
                    Deleted(isDeleted = d.isDeleted, at = d.at)
                }
            )
        }


    suspend fun getUserById(id: String): User {
        val u = api.getUserById(id)
        return User(
            id = u.id,
            name = u.name,
            email = u.email,
            deleted = u.deleted?.let { d -> Deleted(isDeleted = d.isDeleted, at = d.at) }
        )
    }


    suspend fun updateUser(id: String, name: String, email: String): User {
        val req = UpdateUserRequest(name = name, email = email)
        val u = api.updateUser(id, req)
        return User(
            id = u.id,
            name = u.name,
            email = u.email,
            deleted = u.deleted?.let { d -> Deleted(isDeleted = d.isDeleted, at = d.at) }
        )
    }


    suspend fun softDeleteUser(id: String): Boolean =
        try {
            api.softDeleteUser(id).isSuccessful
        } catch (_: Exception) {
            false
        }
}