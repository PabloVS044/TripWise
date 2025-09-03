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
    
}