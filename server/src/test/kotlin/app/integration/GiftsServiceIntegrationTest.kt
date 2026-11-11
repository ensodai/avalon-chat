package app.di

import app.data.db.DatabaseFactory
import app.data.repository.GiftsRepository
import app.domain.gifts.usecases.GetAllGiftPacksUseCase
import app.domain.gifts.usecases.GetMyGiftPacksUseCase
import app.plugins.ApplicationJson
import app.service.GiftsService
import com.ensodai.shared.avalon.gifts.models.GetGiftPacksRequest
import com.ensodai.shared.avalon.gifts.models.GiftPacksResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.uuid.Uuid

class GiftsServiceIntegrationTest : KoinTest {

    private val adminUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")

    private val giftsRepository by inject<GiftsRepository>()
    private val giftsService by inject<GiftsService>()
    private val getAllGiftPacksUseCase by inject<GetAllGiftPacksUseCase>()
    private val getMyGiftPacksUseCase by inject<GetMyGiftPacksUseCase>()

    companion object {
        @BeforeAll
        @JvmStatic
        fun initDb() {
            DatabaseFactory.init()
        }
    }

    @BeforeEach
    fun setup() {
        startKoin {
            allowOverride(true)
            modules(
                AppKoinModule().module(),
                PaymentModule().module(),
                TelegramModule().module(),
                SpecialBindingsModule().module()
            )
        }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `test getAllGiftPacksUseCase and serialization`() = runBlocking {
        println("--> Testing getAllGiftPacksUseCase")
        val response = getAllGiftPacksUseCase.execute(GetGiftPacksRequest(limit = 20, offset = 0))
        println("--> Got ${response.packs.size} packs")
        assertNotNull(response)
        assertTrue(response.packs.isNotEmpty(), "Packs should not be empty")

        // Test serialization
        val json = ApplicationJson.encodeToString(response)
        println("--> Serialized JSON: $json")
        val decoded = ApplicationJson.decodeFromString<GiftPacksResponse>(json)
        assertEquals(response.packs.size, decoded.packs.size)
    }

    @Test
    fun `test getMyGiftPacksUseCase and serialization`() = runBlocking {
        println("--> Testing getMyGiftPacksUseCase for user $adminUserId")
        val response = getMyGiftPacksUseCase.execute(GetGiftPacksRequest(limit = 20, offset = 0), adminUserId)
        println("--> Got ${response.packs.size} packs for user")
        assertNotNull(response)

        // Test serialization
        val json = ApplicationJson.encodeToString(response)
        println("--> Serialized JSON: $json")
        val decoded = ApplicationJson.decodeFromString<GiftPacksResponse>(json)
        assertEquals(response.packs.size, decoded.packs.size)
    }
}
