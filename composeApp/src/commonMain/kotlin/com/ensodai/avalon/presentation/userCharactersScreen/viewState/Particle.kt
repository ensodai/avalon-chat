package com.ensodai.avalon.presentation.userCharactersScreen.viewState

import androidx.compose.ui.graphics.Color

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val alpha: Float,
    val color: Color
)