package com.ensodai.avalon.domain.useCases.commonUseCase


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class ChatSyncMonitor {

    private val _syncingState = MutableStateFlow<Boolean>(false)
    val isSyncing: StateFlow<Boolean> = _syncingState.asStateFlow()


    fun setSyncing(isSyncing: Boolean) {
        _syncingState.value = isSyncing
    }

}