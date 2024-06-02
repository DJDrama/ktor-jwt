package com.dj.routing

import User
import com.dj.routing.request.UserRequest
import com.dj.routing.response.UserResponse
import com.dj.service.UserService
import com.dj.util.authorized
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.userRoute(userService: UserService) {
    post {
        val userRequest = call.receive<UserRequest>()
        val createdUser = userService.save(
            user = userRequest.toModel()
        ) ?: return@post call.respond(message = HttpStatusCode.BadRequest)
        call.response.header(
            name = "id",
            value = createdUser.id.toString(),
        )
        call.respond(
            message = HttpStatusCode.Created
        )
    }
    authenticate {
        authorized("ADMIN") {
            get {
                val users = userService.findAll()
                call.respond(
                    message = users.map(User::toResponse)
                )
            }
        }
    }
    authenticate("another-auth") {
        authorized("USER", "ADMIN") {
            get("/{id}") {
                val id: String = call.parameters["id"]
                    ?: return@get call.respond(message = HttpStatusCode.BadRequest)

                val foundUser = userService.findById(id = id)
                    ?: return@get call.respond(message = HttpStatusCode.NotFound)

                if (foundUser.username != extractPrincipalUsername(call))
                    return@get call.respond(HttpStatusCode.NotFound)

                call.respond(
                    message = foundUser.toResponse()
                )
            }
        }
    }
}

fun extractPrincipalUsername(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()

private fun UserRequest.toModel() = User(
    id = UUID.randomUUID(),
    username = this.username,
    password = this.password,
    role = role,
)

private fun User.toResponse() = UserResponse(
    id = id,
    username = username,
    role = role,
)
