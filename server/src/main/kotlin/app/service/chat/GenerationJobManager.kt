package app.service.chat

import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class GenerationJobManager {
    private val activeJobs = ConcurrentHashMap<Uuid, Job>()

    fun registerJob(sessionId: Uuid, job: Job) {
        // Если уже идет генерация для этой сессии — отменяем старую
        activeJobs[sessionId]?.cancel(CancellationException("New generation started"))
        activeJobs[sessionId] = job

        // Когда джоба завершится (сама или через cancel), убираем из мапы
        job.invokeOnCompletion {
            activeJobs.computeIfPresent(sessionId) { _, currentJob ->
                if (currentJob === job) null else currentJob
            }
        }
    }

    fun stopJob(sessionId: Uuid) {
        activeJobs[sessionId]?.cancel(CancellationException("User requested stop"))
    }
}