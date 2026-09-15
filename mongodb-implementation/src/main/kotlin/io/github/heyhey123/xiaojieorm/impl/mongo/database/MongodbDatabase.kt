package io.github.heyhey123.xiaojieorm.impl.mongo.database

import com.mongodb.ConnectionString
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.mongo.queries.MongoQueries
import io.github.heyhey123.xiaojieorm.impl.mongo.type.MongoDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

/**
 * Mongodb database implementation.
 *
 */
class MongodbDatabase: Database() {

    /**
     * The MongoDB client.
     */
    var client: MongoClient? = null

    /**
     * The MongoDB database, default to "xiaojie-orm".
     */
    var database: MongoDatabase? = null

    override val dataTypes: MongoDataTypes = MongoDataTypes

    override fun doConnect(url: String, user: String, password: String) {
        client = MongoClient.create(
            ConnectionString("mongodb://$user:$password@$url")
        )
        database = client!!.getDatabase("xiaojie-orm")
        queries = MongoQueries(database!!)
    }

    override fun doDisconnect() {
        client?.close()
        database = null
        client = null
    }

    override suspend fun doRegisterTable(table: Table) {
        val mongoDatabase = checkNotNull(database) {
            "Database is not connected. Please connect before registering tables."
        }
        val collection = mongoDatabase.getCollection<Document>(table.name)
        table.primaryKey?.let {
            val key = Indexes.ascending(it.name)
            val options = IndexOptions().unique(true)
            collection.createIndex(key, options)
        }
    }
}
