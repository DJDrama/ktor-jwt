package com.dj

import com.dj.plugins.configureSecurity
import com.dj.plugins.configureSerialization
import com.dj.repository.RefreshTokenRepository
import com.dj.repository.UserRepository
import com.dj.routing.configureRouting
import com.dj.service.JwtService
import com.dj.service.UserService
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureSerialization()

    val userRepository = UserRepository()
    val refreshTokenRepository = RefreshTokenRepository()
    val jwtService = JwtService(application = this, userRepository = userRepository)
    val userService = UserService(
        userRepository = userRepository,
        jwtService = jwtService,
        refreshTokenRepository = refreshTokenRepository
    )

    configureSecurity(jwtService = jwtService)
    configureRouting(userService = userService)

}
