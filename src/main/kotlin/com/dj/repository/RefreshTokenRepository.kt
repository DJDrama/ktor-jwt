package com.dj.repository

class RefreshTokenRepository {

    private val tokens = mutableMapOf<String, String>()

    fun findUsernameByToken(refreshToken: String): String? =
        tokens[refreshToken]

    fun save(refreshToken: String, username: String) {
        tokens[refreshToken] = username
    }
}