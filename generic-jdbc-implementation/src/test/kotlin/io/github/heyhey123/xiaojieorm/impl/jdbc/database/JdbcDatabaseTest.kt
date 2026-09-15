package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import kotlin.test.Test
import kotlin.test.assertFailsWith

class JdbcDatabaseTest {

    @Test
    fun `generic factory requires a driver property before database construction`() {
        assertFailsWith<IllegalArgumentException> { JdbcDatabaseFactory.create(emptyMap()) }
    }
}
