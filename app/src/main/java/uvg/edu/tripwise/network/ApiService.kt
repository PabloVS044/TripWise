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
import uvg.edu.tripwise.data.model.Deleted

// ===== DATA CLASSES =====

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

data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ApiProperty(
    @SerializedName("_id") val _id: String,
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
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val deleted: PropertyDeleted,
    val reviews: List<Map<String, String>>? = null
)

data class PropertyDeleted(
    @SerializedName("is") val `is`: Boolean,
    val at: String? = null
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
    val latitude: Double?,
    val longitude: Double?
)

data class UpdatePropertyRequest(
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    val pricePerNight: Double? = null,
    val capacity: Int? = null,
    val pictures: List<String>? = null,
    val amenities: List<String>? = null,
    val propertyType: String? = null,
    val approved: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class Login(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("_id") val _id: String,
    val token: String,
    val email: String,
    val role: String
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

data class UpdateReservationRequest(
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val payment: Double? = null,
    val persons: Int? = null,
    val days: Int? = null,
    val state: String? = null
)

data class CreateReservationResponse(
    val reservation: ReservationResponse,
    val itinerary: ItineraryResponse?,
    val message: String
)

data class ItineraryResponse(
    @SerializedName("_id") val id: String,
    val restaurants: List<String>,
    val touristicPlaces: List<String>,
    val activities: List<String>,
    val schedules: List<String>,
    val days: List<Int>
)

data class UpdateItineraryRequest(
    val restaurants: List<String>? = null,
    val touristicPlaces: List<String>? = null,
    val activities: List<String>? = null,
    val schedules: List<String>? = null,
    val days: List<Int>? = null
)

// ===== API INTERFACES =====

interface UserApiService {
    // ----- USER ENDPOINTS -----
    @GET("users")
    suspend fun getUsers(): List<ApiUser>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): ApiUser

    @POST("users/createUser")
    suspend fun createUser(@Body request: CreateUserRequest): Response<ApiUser>

    @PUT("users/updateUser/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): ApiUser

    @PUT("users/updatePassword/{id}")
    suspend fun updatePassword(@Path("id") id: String, @Body request: UpdatePasswordRequest): Response<Unit>

    @DELETE("users/deleteUser/{id}")
    suspend fun softDeleteUser(@Path("id") id: String): Response<Unit>

    // ----- AUTHENTICATION ENDPOINTS -----
    @POST("login")
    suspend fun login(@Body login: Login): Response<LoginResponse>

    // ----- PROPERTY ENDPOINTS -----
    @GET("property")
    suspend fun getProperties(): List<ApiProperty>

    @GET("property/{id}")
    suspend fun getPropertyById(@Path("id") id: String): ApiProperty

    @POST("property/createProperty")
    suspend fun createProperty(@Body property: CreatePropertyRequest): Response<ApiProperty>

    @PUT("property/updateProperty/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body request: UpdatePropertyRequest): Response<ApiProperty>

    @DELETE("property/deleteProperty/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>

    @GET("users/{id}/properties")
    suspend fun getOwnerProperties(@Path("id") id: String): List<ApiProperty>

    // ----- RESERVATION ENDPOINTS -----
    @POST("reservation/createReservation")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<CreateReservationResponse>

    @GET("reservation/{id}")
    suspend fun getReservationById(@Path("id") id: String): ReservationResponse

    @GET("reservation/user/{userId}")
    suspend fun getReservationsByUser(@Path("userId") userId: String): List<ReservationResponse>

    @PUT("reservation/updateReservation/{id}")
    suspend fun updateReservation(@Path("id") id: String, @Body request: UpdateReservationRequest): Response<ReservationResponse>

    @DELETE("reservation/deleteReservation/{id}")
    suspend fun deleteReservation(@Path("id") id: String): Response<Unit>

    @GET("reservation/property/{propertyId}")
    suspend fun getReservationsByProperty(@Path("propertyId") propertyId: String): List<ReservationResponse>

    // ----- ITINERARY ENDPOINTS -----
    @GET("itinerary/{id}")
    suspend fun getItineraryById(@Path("id") id: String): ItineraryResponse

    @GET("itinerary/reservation/{reservationId}")
    suspend fun getItineraryByReservation(@Path("reservationId") reservationId: String): ItineraryResponse

    @PUT("itinerary/updateItinerary/{id}")
    suspend fun updateItinerary(@Path("id") id: String, @Body request: UpdateItineraryRequest): Response<ItineraryResponse>

    @DELETE("itinerary/deleteItinerary/{id}")
    suspend fun deleteItinerary(@Path("id") id: String): Response<Unit>

    @GET("itinerary/user/{userId}")
    suspend fun getItinerariesByUser(@Path("userId") userId: String): List<ItineraryResponse>
}

interface PropertyApiService {
    @GET("property")
    suspend fun getProperties(): List<Property>

    @GET("property/{id}")
    suspend fun getPropertyById(@Path("id") id: String): Property

    @POST("property/create")
    suspend fun createProperty(@Body property: CreatePropertyRequest): Response<Property>

    @PUT("property/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body request: UpdatePropertyRequest): Response<Property>

    @DELETE("property/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>
}