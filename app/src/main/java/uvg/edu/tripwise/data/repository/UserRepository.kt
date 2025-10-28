package uvg.edu.tripwise.data.repository


import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UpdatePasswordRequest
import uvg.edu.tripwise.network.UpdateUserRequest


class UserRepository {
    private val api = RetrofitInstance.api


    suspend fun getUsers(): List<User> =
        api.getUsers().map { apiUser ->
            User(
                id = apiUser.id,
                name = apiUser.name,
                email = apiUser.email,
                pfp = apiUser.pfp,
                role = apiUser.role,
                properties = apiUser.properties,
                bookings = apiUser.bookings,
                itineraries = apiUser.itineraries,
                moneyProperty = apiUser.moneyProperty,
                moneyItinerary = apiUser.moneyItinerary,
                interests = apiUser.interests,
                createdAt = apiUser.createdAt,
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
            pfp = u.pfp,
            role = u.role,
            properties = u.properties,
            bookings = u.bookings,
            itineraries = u.itineraries,
            moneyProperty = u.moneyProperty,
            moneyItinerary = u.moneyItinerary,
            interests = u.interests,
            createdAt = u.createdAt,
            deleted = u.deleted?.let { d -> Deleted(isDeleted = d.isDeleted, at = d.at) }
        )
    }


        suspend fun updateUser(id: String, name: String?, email: String, pfp: String?, interests: List<String>?): User {


            val req = UpdateUserRequest(name = name, email = email, pfp = pfp, interests = interests)


            val u = api.updateUser(id, req)


            return User(


                id = u.id,


                name = u.name,


                email = u.email,


                pfp = u.pfp,


                role = u.role,


                properties = u.properties,


                bookings = u.bookings,


                itineraries = u.itineraries,


                moneyProperty = u.moneyProperty,


                moneyItinerary = u.moneyItinerary,


                interests = u.interests,


                createdAt = u.createdAt,


                deleted = u.deleted?.let { d -> Deleted(isDeleted = d.isDeleted, at = d.at) }


            )


        }


    


        suspend fun updatePassword(id: String, currentPassword: String, newPassword: String): Boolean {


            return try {


                val response = api.updatePassword(id, UpdatePasswordRequest(currentPassword, newPassword))


                response.isSuccessful


            } catch (e: Exception) {


                false


            }


        }
}