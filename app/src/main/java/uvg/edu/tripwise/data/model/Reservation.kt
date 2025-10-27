package uvg.edu.tripwise.data.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("_id") val id: String,
    val reservationUser: String,
    val propertyBooked: PropertyBooked?,
    val checkInDate: String,
    val checkOutDate: String,
    val payment: Double,
    val persons: Int,
    val days: Int,
    val createdAt: String,
    val deleted: Deleted
)

data class PropertyBooked(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Double,
    val pictures: List<String>
)
