package com.example

import kotlinx.serialization.Serializable
import org.bson.Document
import com.mongodb.client.model.Filters

// User data class
@Serializable
data class User(
    val id: String,
    val username: String,
    val passwordHash: String
)

object UserRepository {
    // This will be set from Databases.kt
    lateinit var collection: com.mongodb.client.MongoCollection<Document>

    fun insertUser(user: User) {
        val doc = Document()
            .append("_id", user.id)
            .append("username", user.username)
            .append("passwordHash", user.passwordHash)
        collection.insertOne(doc)
    }

    fun findUserByUsername(username: String): User? {
        val doc = collection.find(Filters.eq("username", username)).first() ?: return null
        return User(
            id = doc.getString("_id"),
            username = doc.getString("username"),
            passwordHash = doc.getString("passwordHash")
        )
    }
}

// Test code (run from main or elsewhere)
/*
fun main() {
    val testUser = User("1", "testuser", "hashedpassword")
    UserRepository.insertUser(testUser)
    val found = UserRepository.findUserByUsername("testuser")
    println(found)
}
*/
