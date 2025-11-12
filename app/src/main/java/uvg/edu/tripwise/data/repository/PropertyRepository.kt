package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.data.model.PropertyReviews
import uvg.edu.tripwise.data.model.ReviewItem
import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.ApiReviewsResponse
import uvg.edu.tripwise.network.CreatePropertyRequest
import uvg.edu.tripwise.network.RetrofitInstance
import retrofit2.Response

class PropertyRepository {

    private val api = RetrofitInstance.api

    // Mapper común → dominio
    private fun ApiProperty.toDomain(): Property = Property(
        id = _id,
        name = name,
        description = description,
        location = location,
        pricePerNight = pricePerNight,
        capacity = capacity.toInt(),
        pictures = pictures,
        amenities = amenities,
        propertyType = propertyType,
        owner = owner,
        approved = approved,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        createdAt = createdAt,
        deleted = Deleted(
            isDeleted = deleted.`is`,
            at = deleted.at
        )
    )

    // READ
    suspend fun getProperties(): List<Property> =
        api.getProperties().map { it.toDomain() }

    suspend fun getPropertiesByOwner(ownerId: String): List<Property> =
        api.getOwnerProperties(ownerId).map { it.toDomain() }

    suspend fun getPropertyById(id: String): Property =
        api.getPropertyById(id).toDomain()

    // CREATE
    suspend fun createProperty(
        ownerId: String,
        name: String,
        description: String,
        location: String,
        pricePerNight: Double,
        capacity: Int,
        pictures: List<String>,
        amenities: List<String>,
        propertyType: String,
        latitude: Double? = null,
        longitude: Double? = null,
        approved: String = "pending"
    ): Property {
        val request = CreatePropertyRequest(
            name = name,
            description = description,
            location = location,
            pricePerNight = pricePerNight,
            capacity = capacity,
            pictures = pictures,
            amenities = amenities,
            propertyType = propertyType,
            owner = ownerId,
            approved = approved,
            latitude = latitude,
            longitude = longitude
        )
        val resp = api.createProperty(request)
        if (!resp.isSuccessful || resp.body() == null) {
            throw IllegalStateException("No se pudo crear la propiedad (${resp.code()})")
        }
        return resp.body()!!.toDomain()
    }

    // DELETE
    suspend fun deleteProperty(id: String): Boolean =
        try {
            val resp = api.deleteProperty(id)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }

    // REVIEWS
    suspend fun getReviewsByProperty(propertyId: String): PropertyReviews {
        val resp: Response<ApiReviewsResponse> = api.getReviewsByProperty(propertyId)
        if (!resp.isSuccessful || resp.body() == null) {
            throw IllegalStateException("No se pudieron cargar reseñas (${resp.code()})")
        }
        val response = resp.body()!!

        // Map<String,Int> → Map<Int,Int> a prueba de basura
        val converted = mutableMapOf<Int, Int>()
        response.statistics.scoreDistribution.forEach { (k, v) ->
            k.toIntOrNull()?.let { converted[it] = v }
        }
        // Garantiza claves 1..5 presentes
        for (s in 1..5) if (s !in converted) converted[s] = 0

        return PropertyReviews(
            propertyId = response.property.id,
            propertyName = response.property.name,
            totalReviews = response.statistics.totalReviews,
            averageScore = response.statistics.averageScore,
            scoreDistribution = converted.toSortedMap(compareByDescending { it }),
            reviews = response.reviews.mapIndexed { idx, it ->
                val firstCommentText = it.comments.firstOrNull()?.comment
                ReviewItem(
                    id = it.reviewId.ifBlank { "${response.property.id}_$idx" },
                    userName = it.user.name,
                    userAvatar = it.user.profilePicture,
                    score = it.score,
                    date = it.date,
                    likes = it.likes,
                    commentsCount = it.commentsCount,
                    commentText = firstCommentText  // <- NUEVO
                )
            }

        )
    }

}
