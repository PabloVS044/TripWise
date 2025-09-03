package uvg.edu.tripwise.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ApiUser(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val pfp: String? = null,
    val role: String? = null,
    val properties: List<String>? = null,
    val bookings: List<String>? = null,
    val itineraries: List<String>? = null,
    val moneyProperty: Double? = null,
    val moneyItinerary: Double? = null,
    val interests: List<String>? = null,
    val createdAt: String? = null,
    val deleted: Deleted? = null
)

data class Deleted(
    @SerializedName("is") val isDeleted: Boolean,
    val at: String? = null
)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val pfp: String? = null,
    val role: String? = null,
    val interests: List<String>? = null
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val pfp: String? = null,
    val role: String? = null,
    val interests: List<String>? = null
)

data class ApiProperty(
    @SerializedName("_id") val _id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Double,
    val capacity: Int,
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val deleted: PropertyDeleted
)

data class PropertyDeleted(
    @SerializedName("is") val `is`: Boolean,
    val at: String? = null
)

data class Property(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Double,
    val capacity: Int,
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val isDeleted: Boolean
)

data class Login (
    val email: String,
    val password: String
)

interface UserApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): ApiUser

    @POST("users/createUser")
    suspend fun createUser(@Body request: CreateUserRequest): Response<ApiUser>

    @PUT("users/updateUser/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): ApiUser

    @DELETE("users/deleteUser/{id}")
    suspend fun softDeleteUser(@Path("id") id: String): Response<Unit>

    @GET("property")
    suspend fun getProperties(): List<ApiProperty>

    @GET("property/{id}")
    suspend fun getPropertyById(@Path("id") id: String): ApiProperty

    @POST("property/createProperty")
    suspend fun createProperty(@Body property: ApiProperty): Response<ApiProperty>

    @DELETE("property/deleteProperty/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>

    @POST("login")
    suspend fun login(@Body login: Login): Response<Map<String, String>>
}