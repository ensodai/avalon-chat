package com.ensodai.avalon.utils

expect fun getGoogleAuthCode(): String?
expect fun clearUrlCode()
expect fun getWebRedirectUri(): String