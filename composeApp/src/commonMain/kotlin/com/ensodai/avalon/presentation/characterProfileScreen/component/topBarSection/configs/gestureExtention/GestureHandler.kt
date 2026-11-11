package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.ui.input.pointer.PointerInputChange

interface GestureHandler {
    fun onVerticalDrag(change: PointerInputChange, verticalDragAmount: Float)
    suspend fun onDragEnd()
}
