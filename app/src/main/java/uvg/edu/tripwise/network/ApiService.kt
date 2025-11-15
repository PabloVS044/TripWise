package uvg.edu.tripwise.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.model.Deleted

// ==================== DATA CLASSES ====================
// (ESTA SECCIÓN ESTABA INCOMPLETA EN TU ARCHIVO ORIGINAL)

// ---------- USERS ----------
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

// ---------- PROPERTIES (CLASES FALTANTES) ----------
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
    val reviews: List<ApiReviewItem>? = null
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
    val latitude: Double? = null,
    val longitude: Double? = null
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

// ---------- AUTH (CLASES FALTANTES) ----------
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

// ---------- BUDGET (CLASES FALTANTES) ----------
data class BudgetDistribution(
    val food: Float = 40f,
    val places: Float = 40f,
    val activities: Float = 20f
)

data class DailyBudgets(
    val food: Double = 0.0,
    val places: Double = 0.0,
    val activities: Double = 0.0
)

data class BudgetInfo(
    val totalBudget: Double = 0.0,
    val days: Int = 1,
    val budgetPerDay: Double = 0.0,
    val distribution: BudgetDistribution = BudgetDistribution(),
    val dailyBudgets: DailyBudgets = DailyBudgets()
)

// ---------- LOCATION (CLASES FALTANTES) ----------
data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

// ---------- RESERVATIONS (CLASES FALTANTES) ----------
data class CreateReservationRequest(
    val reservationUser: String,
    val propertyBooked: String,
    val checkInDate: String,
    val checkOutDate: String,
    val payment: Double,
    val persons: Int,
    val days: Int,
    val activityBudget: Double = 0.0,
    val budgetDistribution: BudgetDistribution = BudgetDistribution()
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

// ---------- ITINERARIES (CLASES FALTANTES) ----------
data class ItineraryResponse(
    @SerializedName("_id") val id: String,
    val restaurants: List<String>,
    val touristicPlaces: List<String>,
    val activities: List<String>,
    val schedules: List<String>,
    val days: List<Int>,
    val locations: List<LocationData>? = null,
    val budgetInfo: BudgetInfo? = null
)

data class UpdateItineraryRequest(
    val restaurants: List<String>? = null,
    val touristicPlaces: List<String>? = null,
    val activities: List<String>? = null,
    val schedules: List<String>? = null,
    val days: List<Int>? = null
)

// ----- REVIEW DATA CLASSES (CLASES FALTANTES) -----
data class ReviewResponse(
    @SerializedName("_id") val id: String,
    val userId: String,
    val propertyId: String,
    val score: Int,
    val date: String,
    val likes: Int = 0,
    val comments: List<String> = emptyList()
)

data class CreateReviewRequest(
    val userId: String,
    val propertyId: String,
    val score: Int
)

data class UpdateReviewRequest(
    val score: Int? = null,
    val likes: Int? = null
)

// ---------- PROPERTY RESERVATIONS AGGREGATE (CLASES FALTANTES) ----------
data class PropertyReservationsResponse(
    val property: PropertyInfo,
    val totalReservations: Int,
    val reservations: List<ReservationItem>
)

data class PropertyInfo(
    val id: String,
    val name: String
)

data class ReservationItem(
    val reservationId: String,
    val user: ReservationUser,
    val checkInDate: String,
    val checkOutDate: String,
    val days: Int,
    val persons: Int,
    val payment: Double,
    val state: String,
    val reservationDate: String,
    val hasItinerary: Boolean
)

data class ReservationUser(
    val id: String,
    val name: String,
    val email: String
)

// ---------- REVIEWS (CLASES FALTANTES) ----------
data class ApiReviewsResponse(
    val property: ApiPropertySummary,
    val statistics: ApiStatistics,
    val reviews: List<ApiReviewItem>
)

data class ApiPropertySummary(
    val id: String,
    val name: String
)

data class ApiStatistics(
    val totalReviews: Int,
    val averageScore: Double,
    val scoreDistribution: Map<String, Int>
)

data class ApiReviewItem(
    val reviewId: String,
    val user: ApiUserMini,
    val score: Int,
    val date: String,
    val likes: Int,
    val commentsCount: Int,
    val comments: List<ApiComment>
)

data class ApiUserMini(
    val id: String,
    val name: String,
    val profilePicture: String?
)

data class ApiComment(
    @SerializedName("_id") val id: String,
    val userId: String,
    @SerializedName(value = "comment", alternate = ["text"])
    val comment: String?,
    val date: String
)

// ---------- AVAILABILITY (CLASES FALTANTES) ----------
data class AvailabilityResponse(
    val unavailableDates: List<String>
)

typealias PropertyAvailabilityResponse = AvailabilityResponse

// ---------- CLOUDINARY UPLOAD (LA QUE AÑADIMOS) ----------
data class UploadImageResponse(
    val message: String,
    val url: String,
    val public_id: String
)

// ==================== API INTERFACES ====================

interface UserApiService {

    // ----- USERS -----
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

    // ----- AUTH (ENDPOINT FALTANTE) -----
    @POST("login")
    suspend fun login(@Body login: Login): Response<LoginResponse>

    // ----- PROPERTIES (ENDPOINTS FALTANTES) -----
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

    // ----- RESERVATIONS (ENDPOINTS FALTANTES) -----
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

    @GET("property/reservations/{propertyId}")
    suspend fun getReservationsProperty(@Path("propertyId") propertyId: String): PropertyReservationsResponse

    @GET("reservation/property/{propertyId}")
    suspend fun getReservationsByProperty(@Path("propertyId") propertyId: String): List<ReservationResponse>

    // ----- ITINERARIES (ENDPOINTS FALTANTES) -----
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

    // ----- REVIEWS (ENDPOINT FALTANTE) -----
    @GET("property/reviews/{id}")
    suspend fun getReviewsByProperty(@Path("id") id: String): Response<ApiReviewsResponse>

    // ----- AVAILABILITY (ENDPOINT FALTANTE) -----
    @GET("property/availability/{id}")
    suspend fun getAvailability(@Path("id") propertyId: String): AvailabilityResponse

    // ----- CLOUDINARY UPLOAD (EL QUE AÑADIMOS) -----
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): UploadImageResponse
}

// Interfaz auxiliar para compatibilidad con RetrofitInstance.PropertyApi
interface PropertyApiService {
    @GET("property")
    suspend fun getProperties(): List<Property>

    @GET("property/{id}")
    suspend fun getPropertyById(@Path("id") id: String): Property

    @GET("availability/{id}")
    suspend fun getPropertyAvailability(@Path("id") id: String): PropertyAvailabilityResponse

    @POST("property/create")
    suspend fun createProperty(@Body property: CreatePropertyRequest): Response<Property>

    @PUT("property/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body request: UpdatePropertyRequest): Response<Property>

    @DELETE("property/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>
}

// Interfaz auxiliar para compatibilidad con RetrofitInstance.ReviewApi
interface ReviewApiService {
    @GET("review")
    suspend fun getReviews(): List<ReviewResponse>

    @GET("review/{id}")
    suspend fun getReviewById(@Path("id") id: String): ReviewResponse

    @POST("review/createReview")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ReviewResponse>

    @PUT("review/updateReview/{id}")
    suspend fun updateReview(@Path("id") id: String, @Body request: UpdateReviewRequest): Response<ReviewResponse>

    @DELETE("review/softDeleteReview/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>
}