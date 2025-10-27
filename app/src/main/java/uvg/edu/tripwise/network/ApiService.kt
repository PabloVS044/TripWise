package uvg.edu.tripwise.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import uvg.edu.tripwise.data.model.Property

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

data class CreatePropertyRequest(
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
    val longitude: Double
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val pfp: String? = null,
    val role: String? = null
)

data class ApiProperty(
    @SerializedName("_id") val _id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Double,
    val capacity: Number, // Changed from Int to Number to handle potential float values
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val deleted: PropertyDeleted,
    val reviews: List<Map<String, String>>? = null // Added to match backend schema
)

data class PropertyDeleted(
    @SerializedName("is") val `is`: Boolean,
    val at: String? = null
)

data class Login (
    val email: String,
    val password: String
)

data class CreateReservationRequest(
    val reservationUser: String,
    val propertyBooked: String,
    val checkInDate: String,
    val checkOutDate: String,
    val payment: Double,
    val persons: Int,
    val days: Int
)

data class ReservationResponse(
    @SerializedName("_id") val id: String,
    val reservationUser: ApiUser,
    val propertyBooked: ApiProperty,
    val checkInDate: String,
    val checkOutDate: String,
    val payment: Double,
    val state: String,
    val persons: Int,
    val days: Int,
    val itinerary: ItineraryResponse?
)

data class CreateReservationResponse(
    val reservation: ReservationResponse,
    val itinerary: ItineraryResponse?,
    val message: String
)

data class ItineraryResponse(
    @SerializedName("_id") val id: String,
    val reservationID: String? = null,
    val restaurants: List<String>,
    val touristicPlaces: List<String>,
    val activities: List<String>,
    val schedules: List<String>,
    val days: List<Int>
)

data class Property(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Double,
    val capacity: Number,
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

    @DELETE("property/deleteProperty/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>

    @POST("login")
    suspend fun login(@Body login: Login): Response<Map<String, String>>

    @POST("property/createProperty")
    suspend fun createProperty(@Body property: CreatePropertyRequest): Response<ApiProperty>

    @POST("reservation/createReservation")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<CreateReservationResponse>

    @GET("reservation/{id}")
    suspend fun getReservationById(@Path("id") id: String): ReservationResponse

    @GET("reservation/user/{userId}")
    suspend fun getReservationsByUser(@Path("userId") userId: String): List<ReservationResponse>

    @GET("itinerary/reservation/{reservationId}")
    suspend fun getItineraryByReservation(@Path("reservationId") reservationId: String): ItineraryResponse

    @GET("itinerary/{id}")
    suspend fun getItineraryById(@Path("id") id: String): ItineraryResponse
}

interface PropertyApiService {
    @GET("property")
    suspend fun getProperties(): List<Property>
    @GET("property/{id}")
    suspend fun getPropertyById(@Path("id") id: String): Property
    @POST("property/create")
    suspend fun createProperty(): List<ApiProperty>
    @PUT("property/{id}")
    suspend fun updateProperty(): List<ApiProperty>
    @DELETE("property/{id}")
    suspend fun deleteProperty(id: String): List<ApiProperty>
}