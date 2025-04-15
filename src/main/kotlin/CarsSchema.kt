package com.example

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

@Serializable
data class Car(
    val brandName: String,
    val model: String,
    val number: String,
    val year: Int,
    val color: String? = null,
    val fuelType: String? = null,
    val transmission: String? = null,
    val mileage: Int? = null,
    val price: Double? = null
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Car = json.decodeFromString(document.toJson())
    }
}

class CarService(private val database: MongoDatabase) {
    private val logger = LoggerFactory.getLogger(CarService::class.java)
    var collection: MongoCollection<Document>

    init {
        try {
            // Check if collection exists
            if (!database.listCollectionNames().contains("cars")) {
                logger.info("Creating cars collection")
                database.createCollection("cars")
            }
            collection = database.getCollection("cars")
            logger.info("Connected to cars collection")
        } catch (e: Exception) {
            logger.error("Error initializing cars collection", e)
            throw e
        }
    }

    // Create new car
    suspend fun create(car: Car): String = withContext(Dispatchers.IO) {
        val doc = car.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read a car
    suspend fun read(id: String): Car? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let(Car::fromDocument)
    }

    // Get all cars
    suspend fun getAll(): List<Car> = withContext(Dispatchers.IO) {
        try {
            logger.info("Starting to fetch all cars")
            val cursor = collection.find()
            val cars = mutableListOf<Car>()
            
            cursor.forEach { document ->
                try {
                    val car = Car.fromDocument(document)
                    cars.add(car)
                    logger.info("Added car: ${car.brandName} ${car.model}")
                } catch (e: Exception) {
                    logger.error("Error converting document: ${document.toJson()}", e)
                }
            }
            
            logger.info("Finished fetching cars, found: ${cars.size}")
            cars
        } catch (e: Exception) {
            logger.error("Failed to fetch cars", e)
            emptyList()
        }
    }

    // Update a car
    suspend fun update(id: String, car: Car): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), car.toDocument())
    }

    // Delete a car
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
}
