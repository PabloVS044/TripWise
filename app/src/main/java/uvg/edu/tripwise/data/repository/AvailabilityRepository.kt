package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UserApiService

class AvailabilityRepository(
    private val api: UserApiService = RetrofitInstance.api
) {
    /**
     * Devuelve las fechas ocupadas como Strings "YYYY-MM-DD" (idénticas a la API).
     * Sin java.time para evitar requisitos de API 26.
     */
    suspend fun getUnavailableDates(propertyId: String): Set<String> {
        val resp = api.getAvailability(propertyId)
        return resp.unavailableDates.toSet()
    }
}
