// HostStatsRepository.kt
package uvg.edu.tripwise.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.OwnerStats
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UserApiService

/**
 * Repositorio para consumir /users/{id}/properties-stats
 * y transformar los valores al formato de la UI.
 *
 * Usa RetrofitInstance.api y el método existente:
 *   @GET("users/{id}/properties-stats")
 *   suspend fun getOwnerStats(@Path("id") id: String): Map<String, Any>
 */
class HostStatsRepository(
    private val api: UserApiService = RetrofitInstance.api,
    private val gson: Gson = Gson()
) {

    /** DTO que refleja exactamente la respuesta del endpoint */
    data class OwnerPropertiesStatsResponse(
        val properties: List<ApiProperty>,
        val stats: OwnerStats
    )

    /** Modelo para pintar en las tarjetas de la Home del Host */
    data class HostStatsUi(
        val occupancyPct: Int,   // 0..100
        val revenueMonth: Double,
        val rating: Double,      // 0..5
        val responseRatePct: Int // 0..100
    )

    /**
     * Devuelve sólo las estadísticas normalizadas para UI.
     * - ocupacion: si viene 0..1 -> multiplica x100; si ya viene 0..100, lo respeta.
     * - calificacion: si viene 0..1 -> multiplica x5; si ya viene 1..5, lo respeta.
     * - respuesta: si viene 0..1 -> multiplica x100; si ya viene 0..100, lo respeta.
     */
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

    /** Si además quieres las propiedades que devuelve el endpoint. */
    suspend fun getOwnerProperties(userId: String): List<ApiProperty> =
        fetchAndParse(userId).properties

    // ----------------- Privado -----------------

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
