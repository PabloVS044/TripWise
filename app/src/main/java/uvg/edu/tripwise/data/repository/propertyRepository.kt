package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.Deleted
import uvg.edu.tripwise.data.model.Property
import uvg.edu.tripwise.network.RetrofitInstance

class PropertyRepository {
    private val api = RetrofitInstance.api

    suspend fun getProperties(): List<Property> {
        val response = api.getProperties()
        return response.map { apiProperty ->
            Property(
                id = apiProperty._id,
                name = apiProperty.name,
                description = apiProperty.description,
                location = apiProperty.location,
                pricePerNight = apiProperty.pricePerNight,
                capacity = apiProperty.capacity.toInt(), // Convertir Number a Int
                pictures = apiProperty.pictures,
                amenities = apiProperty.amenities,
                propertyType = apiProperty.propertyType,
                owner = apiProperty.owner,
                approved = apiProperty.approved,
                latitude = apiProperty.latitude,
                longitude = apiProperty.longitude,
                createdAt = apiProperty.createdAt,
                deleted = Deleted( // Mapear PropertyDeleted a Deleted
                    isDeleted = apiProperty.deleted.`is`,
                    at = apiProperty.deleted.at
                )
            )
        }
    }

    suspend fun deleteProperty(id: String): Boolean {
        return try {
            api.deleteProperty(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}