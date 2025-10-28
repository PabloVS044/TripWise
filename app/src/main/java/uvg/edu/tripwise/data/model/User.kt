package uvg.edu.tripwise.data.model

import com.google.gson.annotations.SerializedName

data class User(
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
    val deleted: Deleted?
)