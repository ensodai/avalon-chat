package app.apiv4.services.impl

import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import com.ensodai.shared.avalon.rpc.services.AppVersionCheckResponse
import com.ensodai.shared.avalon.rpc.services.AppVersionRpcService
import com.ensodai.shared.avalon.rpc.services.CheckAppVersionRequest
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent

@Factory
class AppVersionRpcServiceImpl(
    @InjectedParam session: RpcSessionContext
) : BaseRpcService(session), AppVersionRpcService, KoinComponent {

    private val minVersionCode: Long = 84L
    private val actualAppVersion: String = "5.0 beta"

    override suspend fun checkVersion(request: CheckAppVersionRequest): AppVersionCheckResponse {
        val isOutdated = request.versionCode < minVersionCode
        return AppVersionCheckResponse(
            isUpdateRequired = isOutdated,
            minVersionCode = minVersionCode,
            actualAppVersion = actualAppVersion,
            updateUrl = ""
        )
    }
}
