package com.ensodai.avalon.data

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString as getComposeString

class ResourceWrapper {

    suspend fun getString(resource: StringResource, vararg args: Any): String {
        return getComposeString(resource, *args)
    }

    suspend fun getQuantityString(
        resource: PluralStringResource,
        quantity: Int,
        vararg args: Any
    ): String {
        return getPluralString(resource, quantity, *args)
    }
}
