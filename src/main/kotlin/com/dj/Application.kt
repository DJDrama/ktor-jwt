package com.dj

import com.dj.plugins.*
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
    val userService = UserService(repository = userRepository)

    val jwtService = JwtService(application = this, userService = userService)
    configureSecurity(jwtService = jwtService)
    configureRouting(userService = userService, jwtService = jwtService)

}
