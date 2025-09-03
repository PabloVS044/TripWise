package uvg.edu.tripwise.data.model

data class Post(
    val name: String,
    val description: String,
    val location: String,
    val pricePerNight: Int,
    val Capacity: Int,
    val pictures: List<String>,
    val amenities: List<String>,
    val propertyType: String,
    val owner: String,
    val approved: String,
    val reviews: List<String>,
    val latitude: Double,
    val longitude: Double
)