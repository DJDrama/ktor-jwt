package com.dj.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.dj.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(
    private val application: Application,
    private val userRepository: UserRepository
) {

    private val secret = getConfigProperty(path = "jwt.secret")
    private val issuer = getConfigProperty(path = "jwt.issuer")
    private val audience = getConfigProperty(path = "jwt.audience")

    private val algorithm = Algorithm.HMAC256(secret)

    val realm = getConfigProperty(path = "jwt.realm")

    val jwtVerifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun createAccessToken(username: String, role: String): String =
        createJwtToken(username = username, role = role, expireIn = 3_600_000) // 1 hour

    fun createRefreshToken(username: String, role: String): String =
        createJwtToken(username = username, role = role, expireIn = 86_400_00) // 24 hours

    private fun createJwtToken(
        username: String,
        role: String,
        expireIn: Int
    ): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + expireIn))
            .sign(algorithm)

    fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let(userRepository::findByUsername)
        return foundUser?.let {
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    fun audienceMatches(audience: String): Boolean =
        this.audience == audience

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(audience)

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun getConfigProperty(path: String) =
        application.environment.config.property(path).getString()

}