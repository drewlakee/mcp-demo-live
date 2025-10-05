package com.github.drewlakee.mcp.demo.live.datasource.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.codecs.pojo.annotations.BsonId

data class MongoCatalogueSubscription(
    @BsonId
    val subscriptionName: String,
    val features: Set<String>,
)

class MongoSubscriptionsCatalogueDao(
    private val mongoClient: MongoClient = MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
) {
    private val collection: MongoCollection<MongoCatalogueSubscription> =
        mongoClient
            .getDatabase(System.getenv("MONGO_DATABASE"))
            .getCollection("catalogueSubscriptions", MongoCatalogueSubscription::class.java)

    init {
        collection.drop()
        collection.insertMany(listOf(
            MongoCatalogueSubscription(
                subscriptionName = "basic.subscription.tariff",
                features = setOf("music")
            ),
            MongoCatalogueSubscription(
                subscriptionName = "standart.subscription.tariff",
                features = setOf("movies", "music")
            ),
            MongoCatalogueSubscription(
                subscriptionName = "super.subscription.tariff",
                features = setOf("movies", "music", "cloud-gaming")
            )
        ))
    }

    fun getSubscriptionByName(subscriptionName: String) = collection.find(Filters.eq("_id", subscriptionName)).firstOrNull()
    fun getCatalogueSubscriptions() = collection.find().toList()
}