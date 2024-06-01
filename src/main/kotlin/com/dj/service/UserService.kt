package com.dj.service

import User
import com.auth0.jwt.interfaces.DecodedJWT
import com.dj.repository.RefreshTokenRepository
import com.dj.repository.UserRepository
import com.dj.routing.request.LoginRequest
import com.dj.routing.response.AuthResponse
import java.util.*

class UserService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun findAll(): List<User> = userRepository.findAll()

    fun findById(id: String): User? = userRepository.findById(id = UUID.fromString(id))

    fun findByUsername(username: String): User? = userRepository.findByUsername(username = username)

    fun save(user: User): User? {
        val foundUser = findByUsername(user.username)
        return if (foundUser == null) {
            userRepository.save(user = user)
            user
        } else
            null
    }

    fun authenticate(loginRequest: LoginRequest): AuthResponse? {
        val username = loginRequest.username
        val foundUser = userRepository.findByUsername(username = username)

        return if (foundUser != null && foundUser.password == loginRequest.password) {
            val accessToken = jwtService.createAccessToken(username = username)
            val refreshToken = jwtService.createRefreshToken(username = username)

            refreshTokenRepository.save(refreshToken, username)

            return AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } else {
            null
        }
    }

    fun refreshToken(token: String): String? {
        val decodedRefreshToken = verifyRefreshToken(token)
        val persistedUsername = refreshTokenRepository.findUsernameByToken(token)

        return if (decodedRefreshToken != null && persistedUsername != null) {
            val foundUser = userRepository.findByUsername(username = persistedUsername)
            val usernameFromRefreshToken = decodedRefreshToken.getClaim("username").asString()

            if (foundUser != null && usernameFromRefreshToken == foundUser.username) {
                jwtService.createAccessToken(username = persistedUsername)
            } else
                null
        } else
            null
    }

    private fun verifyRefreshToken(token: String): DecodedJWT? {
        val decodedJWT = decodedJWT(token)

        return decodedJWT?.let {
            val audienceMatches = jwtService.audienceMatches(it.audience.first())
            if (audienceMatches)
                decodedJWT
            else
                null
        }
    }

    private fun decodedJWT(token: String) = try {
        jwtService.jwtVerifier.verify(token)
    } catch (e: Exception) {
        null
    }

}