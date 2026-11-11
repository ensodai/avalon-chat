package com.ensodai.avalon.presentation.core

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object Error : LoadingState()
    object Success : LoadingState()
}
