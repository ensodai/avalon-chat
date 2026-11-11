package app.domain.liveCall.models

enum class LiveCallState {
    IDLE,
    STARTING,
    ACTIVE,
    STOPPING,
    STOPPED,
    FAILED
}
