package uvg.edu.tripwise.data.model

data class PropertyReviews(
    val propertyId: String,
    val propertyName: String,
    val totalReviews: Int,
    val averageScore: Double,
    val scoreDistribution: Map<Int, Int>,
    val reviews: List<ReviewItem>
)

data class ReviewItem(
    val id: String,
    val userName: String,
    val userAvatar: String?,
    val score: Int,
    val date: String,
    val likes: Int,
    val commentsCount: Int,
    val commentText: String?    // <- NUEVO
)
