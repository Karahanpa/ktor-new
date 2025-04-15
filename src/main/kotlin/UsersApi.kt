package com.example

import com.example.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.userRoutes() {
    // Register user
    post("/users") {
        val req = call.receive<User>()
        if (UserRepository.findUserByUsername(req.username) != null) {
            call.respond(HttpStatusCode.Conflict, "Username already exists")
            return@post
        }
        val user = req.copy(id = ObjectId().toHexString())
        UserRepository.insertUser(user)
        call.respond(HttpStatusCode.Created, mapOf("id" to user.id))
    }

    // Get user by username (for testing)
    get("/users/{username}") {
        val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val user = UserRepository.findUserByUsername(username)
        if (user != null) {
            call.respond(user)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
