package com.ensodai.avalon.utils

import kotlin.random.Random
import kotlin.time.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun Throwable.messageOrDefault(defaultMessage: String = "Unknown error"): String {
    return message ?: defaultMessage
}

fun randomProbability(): Double = Random.nextDouble()
