package io.github.heyhey123.skriptorm.impl.mongo.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.github.heyhey123.skriptorm.database.ConnectionSettings
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.impl.mongo.DOCUMENT_ID
import io.github.heyhey123.skriptorm.impl.mongo.queries.MongoQueries
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoDataTypes
import io.github.heyhey123.skriptorm.table.Table
import java.time.Duration

/**
 * Mongodb database implementation.
 *
 * @param properties what the `create a connection` section declared apart from the url and the
 *   credentials. That is how the database to use, and the database the credentials belong to, are named:
 *   see [MongoConnection].
 */
class MongodbDatabase(
    private val properties: Map<String, String> = emptyMap()
) : Database() {

    /**
     * How long one statement may take, read once so that an unusable value is refused when the
     * connection is created rather than at the first statement.
     */
    private val statementTimeoutSeconds: Int = MongoConnection.statementTimeoutSeconds(properties)

    /**
     * The MongoDB client.
     */
    var client: MongoClient? = null

    /**
     * The MongoDB database this connection uses.
     */
    var database: MongoDatabase? = null

    override val dataTypes: MongoDataTypes = MongoDataTypes

    /**
     * Longer than a statement may take, so that a statement which is about to time out on its own does:
     * it fails, releases its lease, and the disconnect it interrupted goes on to close the client as usual.
     */
    override val closeWaitTimeout: Duration
        get() = if (statementTimeoutSeconds > 0) {
            Duration.ofSeconds(statementTimeoutSeconds.toLong() + DRAIN_MARGIN_SECONDS)
        } else {
            DEFAULT_CLOSE_WAIT_TIMEOUT
        }

    override fun doConnect(settings: ConnectionSettings) {
        val databaseName = MongoConnection.database(properties, settings.url)
        val opened = MongoClients.create(MongoConnection.clientSettings(settings, databaseName, properties))
        client = opened
        database = opened.getDatabase(databaseName)
        queries = MongoQueries(database!!)
    }

    override fun doDisconnect() {
        client?.close()
        database = null
        client = null
    }

    /**
     * Declares [table] to the database.
     *
     * A collection needs no declaration the way a SQL table does, since MongoDB creates it with the
     * first document written to it, so what is declared here is what a collection cannot be given
     * later: the unique index that makes the primary key a key, and the counter of an auto-increment
     * column. Everything else a column declares — nullability, size — has no MongoDB equivalent and is
     * therefore not enforced by the server.
     *
     * Registering a table twice is allowed and changes nothing: creating an existing index and setting
     * a value on an insert-only update are both no-ops.
     */
    override suspend fun doRegisterTable(table: Table) {
        val mongoDatabase = checkNotNull(database) {
            "Database is not connected. Please connect before registering tables."
        }
        require(table.name != MongoSequences.COLLECTION) {
            "Table ${table.name} cannot be named '${MongoSequences.COLLECTION}': that collection holds the counters of auto-increment columns."
        }
        table.columns.values.forEach { column ->
            require(column.name != DOCUMENT_ID) {
                "Table ${table.name} cannot declare a column called '$DOCUMENT_ID': MongoDB reserves that name for the identifier of the document itself."
            }
        }
        val collection = mongoDatabase.getCollection(table.name)
        table.primaryKey?.let {
            collection.createIndex(Indexes.ascending(it.name), IndexOptions().unique(true))
        }
        MongoSequences.prepare(mongoDatabase, table)
    }

    private companion object {

        /** How much longer a disconnect waits than a statement may take, so that a timeout wins. */
        private const val DRAIN_MARGIN_SECONDS = 5L
    }
}
