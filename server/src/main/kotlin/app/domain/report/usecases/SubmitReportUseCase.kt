package app.domain.report.usecases

import com.ensodai.shared.avalon.session.models.ReportRequest
import app.service.ReportService
import com.github.kotlintelegrambot.Bot
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class SubmitReportUseCase(
    private val reportService: ReportService,
    private val bot: Bot,
    private val adminId: Long
) {
    suspend fun execute(request: ReportRequest, userId: Uuid): String {
        val characterUUID = request.characterId

        return if (userId in reportService.getModeratorIds()) {
            reportService.handleModeratorCommand(request, userId, characterUUID, bot, adminId)
        } else {
            reportService.handleUserReport(request, userId, characterUUID, bot, adminId)
            "Report submitted"
        }
    }
}
