package app.service.liveCall

import app.service.VertexAuthData
import app.service.withConfiguredProxy
import com.google.genai.Client
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

import org.koin.core.annotation.Single

@Single
class VertexLiveClientProvider(
    private val vertexAuthData: VertexAuthData
) {

    private val cachedClients = ConcurrentHashMap<String, Client>()
    private val clientMutex = Mutex()

    suspend fun getClient(location: String): Client {
        cachedClients[location]?.let { return it }

        return clientMutex.withLock {
            cachedClients[location]?.let { return it }

            Client.builder()
                .vertexAI(true)
                .project(vertexAuthData.projectId)
                .credentials(vertexAuthData.credentials)
                .location(location)
                .withConfiguredProxy()
                .build()
                .also { createdClient ->
                    cachedClients[location] = createdClient
                }
        }
    }
}
