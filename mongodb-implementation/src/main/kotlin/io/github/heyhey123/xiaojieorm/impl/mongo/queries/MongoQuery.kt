package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase

interface MongoQuery {

    val database: MongoDatabase
}
