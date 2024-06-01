package com.dj.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.dj.routing.request.LoginRequest
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(
    private val application: Application,
    private val userService: UserService
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

    fun createJwtToken(loginRequest: LoginRequest): String? {
        val foundUser = userService.findByUsername(username = loginRequest.username)

        return if (foundUser != null && foundUser.password == loginRequest.password) {
            JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", foundUser.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
                .sign(algorithm)
        } else {
            null
        }
    }

    fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let(userService::findByUsername)
        return foundUser?.let {
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(audience)

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun getConfigProperty(path: String) =
        application.environment.config.property(path).getString()

}