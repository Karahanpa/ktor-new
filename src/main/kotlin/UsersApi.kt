package com.example

import com.example.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import kotlinx.serialization.Serializable

// Registration request with validation
@Serializable
data class RegistrationRequest(
    val username: String,
    val password: String
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (username.length !in 3..20 || !username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            errors.add("Invalid username. Must be 3-20 characters, letters, numbers, or underscores.")
        }
        if (password.length < 8 ||
            !password.any { it.isUpperCase() } ||
            !password.any { it.isLowerCase() } ||
            !password.any { it.isDigit() }
        ) {
            errors.add("Password must be at least 8 characters and include uppercase, lowercase and digit.")
        }
        return errors
    }
}

fun Route.userRoutes() {
    // Register user
    post("/users") {
        val req = call.receive<RegistrationRequest>()
        val errors = req.validate()
        if (errors.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to errors))
            return@post
        }
        if (UserRepository.findUserByUsername(req.username) != null) {
            call.respond(HttpStatusCode.Conflict, "Username already exists")
            return@post
        }
        val user = User(id = org.bson.types.ObjectId().toHexString(), username = req.username, passwordHash = req.password) // TODO: Hash password!
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
