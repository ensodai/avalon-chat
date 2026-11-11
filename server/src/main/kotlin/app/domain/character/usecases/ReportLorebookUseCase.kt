package app.domain.character.usecases

import app.service.ReportService
import com.ensodai.shared.avalon.character.models.ReportLorebookRequest
import com.ensodai.shared.avalon.session.models.ReportResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class ReportLorebookUseCase(
    private val reportService: ReportService
) {
    suspend fun execute(request: ReportLorebookRequest, reporterUserId: Uuid): ReportResponse {
        val result = reportService.handleLorebookReport(
            lorebookId = request.lorebookId,
            reporterUserId = reporterUserId,
            message = request.message
        )
        return ReportResponse(message = result)
    }
}
