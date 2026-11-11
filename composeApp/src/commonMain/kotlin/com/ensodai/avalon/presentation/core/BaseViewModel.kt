package com.ensodai.avalon.presentation.core

import androidx.lifecycle.ViewModel
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

abstract class BaseViewModel<S : BaseViewState, A : BaseActions>(
    initialState: S
) : ViewModel() {

    private val _viewState = MutableStateFlow(initialState)

    val viewState: StateFlow<S> = _viewState.asStateFlow()

    abstract val actions: A

    fun updateViewState(block: (S) -> S) {
        _viewState.getAndUpdate(block)
    }
}

// Для действий без параметров: () -> Unit
fun (() -> Unit).withLog(name: String): () -> Unit {
    return {
        L.d("BaseViewModel", "ACTION: $name")
        this() // Вызываем оригинальную лямбду
    }
}

// Для действий с одним параметром: (T) -> Unit
fun <T> ((T) -> Unit).withLog(name: String): (T) -> Unit {
    return { param ->
        L.d("BaseViewModel", "ACTION: $name | Payload: $param")
        this(param) // Вызываем оригинальную лямбду с параметром
    }
}
