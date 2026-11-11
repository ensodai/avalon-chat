package com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase

import com.ensodai.shared.avalon.rpc.services.SessionSupportRpcService
import com.ensodai.shared.avalon.session.models.ReportRequest
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ReportCharacterUseCase(
    private val sessionSupportRpcService: SessionSupportRpcService
) {

    suspend fun report(characterId: Uuid, message: String): Result<Unit> {
        return runCatching {
            sessionSupportRpcService.submitReport(
                ReportRequest(
                    characterId = characterId,
                    message = message
                )
            )
            Unit
        }
    }
}
