package uvg.edu.tripwise.data.model

import com.google.gson.annotations.SerializedName

data class Property(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String,
    val location: String,
    @SerializedName("pricePerNight") val pricePerNight: Double,
    val capacity: Int,
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val deleted: Deleted
)