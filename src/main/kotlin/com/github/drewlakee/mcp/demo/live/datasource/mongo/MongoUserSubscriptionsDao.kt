package com.github.drewlakee.mcp.demo.live.datasource.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.UpdateOptions
import org.bson.codecs.pojo.annotations.BsonId

data class MongoUserSubscription(
    @BsonId
    val id: Int,
    val uid: Int,
    val subscriptionName: String,
    val features: Set<String>,
)

class MongoUserSubscriptionsDao(
    private val mongoClient: MongoClient = MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
) {
    private val collection: MongoCollection<MongoUserSubscription> =
        mongoClient
            .getDatabase(System.getenv("MONGO_DATABASE"))
            .getCollection("userSubscriptions", MongoUserSubscription::class.java)

    init {
        collection.drop()
        collection.insertMany(listOf(
            MongoUserSubscription(
                id = 1,
                uid = 708,
                subscriptionName = "standart.subscription.tariff",
                features = setOf("movies", "music")
            )
        ))
    }

    fun getByUid(uid: Int): List<MongoUserSubscription> = collection.find(Filters.eq("uid", uid)).toList()
}