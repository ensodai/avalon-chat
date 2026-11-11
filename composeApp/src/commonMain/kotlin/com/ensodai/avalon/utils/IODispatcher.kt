package com.ensodai.avalon.utils

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Кроссплатформенный диспатчер для IO операций
 * На Android/iOS использует IODispatcher
 * На JS/Wasm использует Dispatchers.Default (т.к. IO не доступен)
 */
expect val IODispatcher: CoroutineDispatcher
