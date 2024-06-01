package com.dj.routing

import com.dj.routing.request.LoginRequest
import com.dj.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(jwtService: JwtService) {
    post {
        val loginRequest = call.receive<LoginRequest>()

        val token = jwtService.createJwtToken(loginRequest = loginRequest)
        token?.let {
            call.respond(message = hashMapOf("token" to it))
        } ?: call.respond(message = HttpStatusCode.Unauthorized)
    }
}