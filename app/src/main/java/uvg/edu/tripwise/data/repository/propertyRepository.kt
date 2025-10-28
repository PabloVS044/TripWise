package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.CreatePropertyRequest
import uvg.edu.tripwise.network.RetrofitInstance

class PropertyRepository {

    private val api = RetrofitInstance.api

    // --- Mapper a dominio (re-usa en todos los métodos) ---
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
        latitude = latitude ?: 0.0,   // null-safe por datos faltantes
        longitude = longitude ?: 0.0, // null-safe por datos faltantes
        createdAt = createdAt,
        deleted = Deleted(
            isDeleted = deleted.`is`, // backticks por key "is"
            at = deleted.at
        )
    )

    // ---------------------- READ ----------------------
    suspend fun getProperties(): List<Property> =
        api.getProperties().map { it.toDomain() }

    suspend fun getPropertiesByOwner(ownerId: String): List<Property> =
        api.getOwnerProperties(ownerId).map { it.toDomain() }

    suspend fun getPropertyById(id: String): Property =
        api.getPropertyById(id).toDomain()

    // ---------------------- CREATE ----------------------
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

    // ---------------------- DELETE ----------------------
    suspend fun deleteProperty(id: String): Boolean =
        try {
            val resp = api.deleteProperty(id)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }


}
