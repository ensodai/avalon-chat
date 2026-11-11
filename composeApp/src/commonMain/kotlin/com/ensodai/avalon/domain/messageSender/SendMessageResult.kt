package com.ensodai.avalon.domain.messageSender

sealed class SendMessageResult {

    data class Success(val successGeneratedMessage: SuccessGeneratedMessage) : SendMessageResult()

    object SuccessSystemMessage : SendMessageResult()

}

sealed class SendMessageResultFailure : Throwable() {
    class NoEnableUserApiKeys : SendMessageResultFailure() {
        private fun readResolve(): Any = NoEnableUserApiKeys()
    }

    class UserStopGeneratingMessage : SendMessageResultFailure() {
        private fun readResolve(): Any = UserStopGeneratingMessage()
    }

    data class GoogleServerException(val exception: Throwable) : SendMessageResultFailure()

    class GoogleApiRetryLimitExceeded : SendMessageResultFailure() {
        private fun readResolve(): Any = GoogleApiRetryLimitExceeded()
    }

    class ResourceExhaustedException : SendMessageResultFailure() {
        private fun readResolve(): Any = ResourceExhaustedException()
    }

    class DisabledAutoreplaceApiKeySettings : SendMessageResultFailure() {
        private fun readResolve(): Any = DisabledAutoreplaceApiKeySettings()
    }

    class SystemApiKeyQuotaLimit : SendMessageResultFailure() {
        private fun readResolve(): Any = SystemApiKeyQuotaLimit()
    }

    class UpgradeRequired : SendMessageResultFailure() {
        private fun readResolve(): Any = UpgradeRequired()
    }

    class AutoReplaceSystemApiKeyFail : SendMessageResultFailure() {
        private fun readResolve(): Any = AutoReplaceSystemApiKeyFail()
    }

    class GetSystemApiKeyFail : SendMessageResultFailure() {
        private fun readResolve(): Any = GetSystemApiKeyFail()
    }

    class GoogleApiUnexpectedDataException : SendMessageResultFailure() {
        private fun readResolve(): Any = GoogleApiUnexpectedDataException()
    }
}
