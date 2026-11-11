package app.domain.system.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.gifts.models.*
import app.service.GeoService

@Factory
class GetUserLocationUseCase(
    private val geoService: GeoService
) {
    suspend fun execute(ip: String): LocationResultResponse {
        return try {
            val countryCode = geoService.resolveCountry(ip)

            val recommendRuStore = countryCode == "RU" || countryCode == "BY"

            LocationResultResponse(
                countryCode = countryCode ?: "UNKNOWN",
                isRuStoreRecommended = recommendRuStore
            )
        } catch (e: Exception) {
            throw Exception("Failed to get user location: ${e.message}")
        }
    }
}
