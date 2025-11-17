package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UserApiService

class AvailabilityRepository(
    private val api: UserApiService = RetrofitInstance.api
) {

    suspend fun getUnavailableDates(propertyId: String): Set<String> {
        val resp = api.getAvailability(propertyId)
        return resp.unavailableDates.toSet()
    }
}
