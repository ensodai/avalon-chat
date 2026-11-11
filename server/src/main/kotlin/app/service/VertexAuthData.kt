package app.service

import com.google.auth.oauth2.GoogleCredentials

data class VertexAuthData(
    val projectId: String,
    val credentials: GoogleCredentials
)