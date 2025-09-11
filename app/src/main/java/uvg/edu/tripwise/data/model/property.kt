package uvg.edu.tripwise.data.model

import com.google.gson.annotations.SerializedName

data class PropertyDeleted(
    @SerializedName("is") val `is`: Boolean,
    val at: String? = null
)

data class Post(
    @SerializedName("_id") val _id: String,
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Int,
    val capacity: Int,
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val reviews: List<String>,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val isDeleted: Boolean
)