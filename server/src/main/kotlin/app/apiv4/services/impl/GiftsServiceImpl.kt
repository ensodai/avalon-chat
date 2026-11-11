package app.apiv4.services.impl

import app.domain.gifts.usecases.CreateGiftItemUseCase
import app.domain.gifts.usecases.CreateGiftPackUseCase
import app.domain.gifts.usecases.GetAllGiftPacksUseCase
import app.domain.gifts.usecases.GetMyGiftPacksUseCase
import app.service.paymentService.PaymentService
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.rpc.services.GiftsRpcService
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger("GiftsRpcService")

@Factory
class GiftsServiceImpl(
    @InjectedParam sessionContext: RpcSessionContext,
    private val createGiftPackUseCase: CreateGiftPackUseCase,
    private val createGiftItemUseCase: CreateGiftItemUseCase,
    private val getMyGiftPacksUseCase: GetMyGiftPacksUseCase,
    private val getAllGiftPacksUseCase: GetAllGiftPacksUseCase,
    private val paymentService: PaymentService
) : BaseRpcService(sessionContext), GiftsRpcService, KoinComponent {

    override suspend fun createGiftPack(request: CreateGiftPackRequest): GiftPackResponse {
        return try {
            ensureAuthorized()
            val userId = userId()
            log.info { "🎁 createGiftPack: user=$userId name=${request.name}" }
            createGiftPackUseCase.execute(request, userId)
        } catch (e: Throwable) {
            log.error(e) { "❌ createGiftPack FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun createGiftItem(request: CreateGiftItemRequest): GiftItemResponse {
        return try {
            ensureAuthorized()
            log.info { "🎁 createGiftItem: packId=${request.packId} giftName=${request.giftName}" }
            createGiftItemUseCase.execute(request)
        } catch (e: Throwable) {
            log.error(e) { "❌ createGiftItem FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun getMyGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse {
        return try {
            ensureAuthorized()
            val userId = userId()
            log.info { "🎁 getMyGiftPacks: user=$userId limit=${request.limit} offset=${request.offset}" }
            val response = getMyGiftPacksUseCase.execute(request, userId)
            log.info { "🎁 getMyGiftPacks: user=$userId returned ${response.packs.size} packs" }
            response
        } catch (e: Throwable) {
            log.error(e) { "❌ getMyGiftPacks FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAllGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse {
        return try {
            ensureAuthorized()
            log.info { "🎁 getAllGiftPacks: limit=${request.limit} offset=${request.offset}" }
            val response = getAllGiftPacksUseCase.execute(request)
            log.info { "🎁 getAllGiftPacks: returned ${response.packs.size} packs" }
            response
        } catch (e: Throwable) {
            log.error(e) { "❌ getAllGiftPacks FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun validateSubscription(request: ValidateSubscriptionRequest): SubscriptionStatusResponse {
        return try {
            ensureAuthorized()
            val userId = userId()
            log.info { "💳 validateSubscription: user=$userId productId=${request.productId}" }
            val result = paymentService.validateSubscription(
                userId = userId,
                purchaseId = request.purchaseId,
                productId = request.productId,
                provider = request.provider,
                isSandbox = request.isSandbox
            )
            result.getOrThrow()
        } catch (e: Throwable) {
            log.error(e) { "❌ validateSubscription FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun validateConsumable(request: ValidateConsumableRequest): ConsumableResultResponse {
        return try {
            ensureAuthorized()
            val userId = userId()
            log.info { "💳 validateConsumable: user=$userId productId=${request.productId}" }
            val result = paymentService.consumeProduct(
                userId = userId,
                transactionId = request.transactionId,
                productId = request.productId,
                provider = request.provider,
                isSandbox = request.isSandbox
            )
            result.getOrThrow()
        } catch (e: Throwable) {
            log.error(e) { "❌ validateConsumable FAILED: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAvailableProducts(): ProductsListResponse {
        return try {
            ensureAuthorized()
            log.info { "💳 getAvailableProducts" }
            val result = paymentService.getAvailableProducts()
            result.getOrThrow()
        } catch (e: Throwable) {
            log.error(e) { "❌ getAvailableProducts FAILED: ${e.message}" }
            throw e
        }
    }
}
