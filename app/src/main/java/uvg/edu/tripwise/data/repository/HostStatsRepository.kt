// HostStatsRepository.kt
package uvg.edu.tripwise.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.OwnerStats
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UserApiService

class HostStatsRepository(
    private val api: UserApiService = RetrofitInstance.api,
    private val gson: Gson = Gson()
) {


    data class OwnerPropertiesStatsResponse(
        val properties: List<ApiProperty>,
        val stats: OwnerStats
    )

    data class HostStatsUi(
        val occupancyPct: Int,
        val revenueMonth: Double,
        val rating: Double,
        val responseRatePct: Int
    )

    suspend fun getStatsUi(userId: String): HostStatsUi {
        val parsed = fetchAndParse(userId)

        val s = parsed.stats

        val occupancyPct = normalizePercent(s.ocupacion)
        val responseRatePct = normalizePercent(s.respuesta)
        val rating = normalizeRating(s.calificacion)

        return HostStatsUi(
            occupancyPct = occupancyPct,
            revenueMonth = s.ingresosMes,
            rating = rating,
            responseRatePct = responseRatePct
        )
    }

    suspend fun getOwnerProperties(userId: String): List<ApiProperty> =
        fetchAndParse(userId).properties

    private fun normalizePercent(value: Double): Int {
        val pct = if (value <= 1.0) value * 100.0 else value
        return pct.coerceIn(0.0, 100.0).toInt()
    }

    private fun normalizeRating(value: Double): Double {
        val r = if (value <= 1.0) value * 5.0 else value
        return r.coerceIn(0.0, 5.0)
    }

    private suspend fun fetchAndParse(userId: String): OwnerPropertiesStatsResponse {
        // Tu método actual devuelve Map<String, Any>. Lo convertimos a JSON y luego a DTO tipado.
        val raw: OwnerPropertiesStatsResponse = api.getOwnerStats(userId)
        val json = gson.toJson(raw)
        val type = object : TypeToken<OwnerPropertiesStatsResponse>() {}.type
        return gson.fromJson(json, type)
    }
}
