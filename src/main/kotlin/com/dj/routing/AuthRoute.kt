package com.dj.routing

import com.dj.routing.request.LoginRequest
import com.dj.routing.request.RefreshTokenRequest
import com.dj.routing.response.AuthResponse
import com.dj.routing.response.RefreshTokenResponse
import com.dj.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(userService: UserService) {
    post {
        val loginRequest = call.receive<LoginRequest>()

        val authResponse: AuthResponse? =
            userService.authenticate(loginRequest = loginRequest)


        authResponse?.let {
            call.respond(message = it)
        } ?: call.respond(message = HttpStatusCode.Unauthorized)
    }

    post("/refresh") {
        val request = call.receive<RefreshTokenRequest>()

        val newAccessToken: String? = userService.refreshToken(request.token)

        newAccessToken?.let {
            call.respond(
                RefreshTokenResponse(
                    token = it
                )
            )
        } ?: call.respond(message = HttpStatusCode.Unauthorized)
    }
}