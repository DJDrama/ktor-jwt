package com.dj.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val token: String
)
