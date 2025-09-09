package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.data.model.Post
import uvg.edu.tripwise.network.PropertyApiService
import uvg.edu.tripwise.network.RetrofitInstance

class propertyRepository (
    private val api: PropertyApiService = RetrofitInstance.PropertyApi
){
    suspend fun getProperties(): List<Post> {
        val response = api.getProperties()
        return response
    }

    suspend fun getPropertyById(id: String): Post {
        val property = api.getPropertyById(id)
        return property
    }

    suspend fun deleteProperty(id: String): Boolean {
        return try {
            api.deleteProperty(id) // Asume que PropertyApiService tiene un método deleteProperty(id: String)
            true
        } catch (e: Exception) {
            false // En caso de error, devuelve false sin lanzar excepción para manejar en la UI
        }
    }
}