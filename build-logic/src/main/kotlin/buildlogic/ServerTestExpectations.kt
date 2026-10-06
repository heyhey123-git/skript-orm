package buildlogic

class ServerTestExpectations(
    private val serverTestUsesDatabase: Boolean,
    private val serverTestImplementation: ServerTestImplementation
) {
    val serverTestExpectedMessage = if (serverTestUsesDatabase) "" else "No database connected."

    // What a section reports where the implementation has no transactions. The wording is core's, the one
    // `Database.beginTransaction` refuses with, so this is a copy of a message rather than of a decision.
    val serverTestNoTransactionsMessage = "This database implementation does not support transactions."

    // The names only a run with a database reaches, in a map of their own: the scripts that report them are
    // copied only when a database is configured, while the consistency check below has to know them in both
    // modes.
    val serverTestDatabaseChecks = mapOf(
        // The all-form, driven by the disconnect element once nothing else needs a connection.
        "disconnect all" to "ran",
        "setup" to "",
        // What the setup's own read reported: empty means the table it registered is known to the
        // connection, and "Table ... not found." would mean registration did not take effect.
        "setup select" to "",
        "roundtrip" to "",
        // The round trip prints what each read returned, so a failure says whether the row exists as the
        // steps ran, whether it appears after a plain wait, and whether a `where` finds it.
        "roundtrip now" to "",
        "roundtrip later" to "",
        "roundtrip by id" to "",
        // The same read through a literal id, which is the one `%object%` slot a script can fill with a
        // value Skript has not typed yet.
        "roundtrip literal id" to "",
        // Named connections. Only the named connection registered `orm_secondary`, so the read outside the
        // scope has to report the table lookup failing while the scoped one succeeds: that difference is
        // what proves the scope chose a connection at all.
        "connections create" to "",
        "connections register" to "",
        "connections write" to "",
        "connections unscoped" to "Table 'orm_secondary' not found.",
        "connections scoped" to "",
        "connections default" to "",
        // `disconnect` without a name closes the connection in effect. The two lines after it say what it
        // closed: the default still answers for its own table, and the named connection is gone.
        "connections disconnect" to "ran",
        "connections default after" to "",
        // The refusal has two shapes, and which one is written depends on whether any connection is named at
        // that moment — a fact belonging to whichever other script has run by then, not to this one. What this
        // script owns is that the name is reported as unknown, so that much is checked and the rest is left to
        // `ConnectionScopeTest`, which pins both shapes directly.
        "connections gone" to "No connection named 'secondary'.",
        // `use connection` switches for the rest of the event, and the connection it names has no table of
        // its own: the lookup failing is what shows the switch reached it. The named disconnect then closes
        // that same connection, whatever the switch says.
        "connections create tertiary" to "",
        "connections used" to "Table 'orm_roundtrip' not found.",
        "connections named disconnect" to "ran",
        "connections tertiary gone" to "No connection named 'tertiary'.",
        // Transactions. `row1` is the row the first transaction committed and `row2` is `<none>` as long as
        // nothing else survived, so one line says both that a commit worked and that a rollback did.
        "transaction commit" to "",
        "transaction failed" to "Table 'orm_transaction_missing' not found.",
        "transaction rows" to "row1=committed row2=<none>",
        "transaction rollback" to "",
        "transaction rollback rows" to "row1=committed row2=<none>",
        "transaction timeout" to
            "The database transaction was open for longer than 2 seconds and was rolled back.",
        "transaction timeout rows" to "row1=committed row2=<none>",
        "transaction refuse" to
            "A table cannot be registered inside a database transaction, because creating it would commit that transaction."
    )

    // What those same transaction lines report on an implementation that has no transactions at all. The
    // section refuses to open one, skips its body and carries on with the statement after it, so the script
    // still reaches its end and reports the same names — with the refusal instead of a commit, and with no
    // rows for the reads, since nothing the bodies would have written ran.
    val serverTestNoTransactionChecks = mapOf(
        "transaction commit" to serverTestNoTransactionsMessage,
        "transaction failed" to serverTestNoTransactionsMessage,
        "transaction rows" to "row1=<none> row2=<none>",
        "transaction rollback" to serverTestNoTransactionsMessage,
        "transaction rollback rows" to "row1=<none> row2=<none>",
        "transaction timeout" to serverTestNoTransactionsMessage,
        "transaction timeout rows" to "row1=<none> row2=<none>",
        "transaction refuse" to serverTestNoTransactionsMessage
    )

    val serverTestChecks = buildMap {
        put("register table", serverTestExpectedMessage)
        // Re-registering the same table on the same connection succeeds and reports nothing, because that is
        // what reloading a script whose tables are declared in `on load` does. Without a database it stops at
        // the connection lookup first, exactly as the line above it does.
        put("register table again", serverTestExpectedMessage)
        // This one names an implementation that is not installed, so it reports the same in both modes.
        put("create connection", "Database 'NoSuchDatabase' is not supported.")
        put("insert one", serverTestExpectedMessage)
        put("insert many", serverTestExpectedMessage)
        put("insert one from variable", serverTestExpectedMessage)
        put("insert if absent", serverTestExpectedMessage)
        put("select one", serverTestExpectedMessage)
        // The same statements written without a colon, which is what the effect forms are for. The names carry
        // `unfiltered` because the section forms keep their own elements, and a script records itself under a
        // name of its own.
        put("select one unfiltered", serverTestExpectedMessage)
        put("select many unfiltered", serverTestExpectedMessage)
        put("select page unfiltered", serverTestExpectedMessage)
        put("select by id unfiltered", serverTestExpectedMessage)
        put("insert one from variable without colon", serverTestExpectedMessage)
        put("insert many from variable without colon", serverTestExpectedMessage)
        put("insert if absent without colon", serverTestExpectedMessage)
        put("update by id without colon", serverTestExpectedMessage)
        put("upsert by id without colon", serverTestExpectedMessage)
        put("delete by id without colon", serverTestExpectedMessage)
        put("delete entities without colon", serverTestExpectedMessage)
        put("select many", serverTestExpectedMessage)
        put("select page", serverTestExpectedMessage)
        put("select by id", serverTestExpectedMessage)
        put("delete entities", serverTestExpectedMessage)
        put("delete by id", serverTestExpectedMessage)
        put("update entities", serverTestExpectedMessage)
        put("update by id", serverTestExpectedMessage)
        put("upsert by id", serverTestExpectedMessage)
        // Disconnecting has no error channel: it proves it ran by the trigger reaching the end.
        put("disconnect", "ran")
        // The generic `"JDBC"` type on the SQLite driver Paper carries. It reports its verdict as a word
        // rather than an error message, because it asserts what it read back and says `failed` when any of
        // it does not hold, in both modes.
        put("sqlite round trip", "ok")
        // A number written to a text column is written as its digits rather than refused: Skript cannot parse
        // a number as text, so the value is parsed as a number and written out here. It reports a word rather
        // than an error message for the same reason the round trip above does.
        put("sqlite number into string", "ok")
        // The raw statements on the same SQLite connection: their verdict is a word too, because they assert
        // both what the server returned and that each guard refused before anything was sent. They run in
        // both modes for the same reason the round trip does — they need no database server.
        put("raw sql", "ok")
        // The row ceiling, also on SQLite: a read past it is refused, and a write past it is sent as several
        // statements so that every row is written. It fills its own table with more rows than the ceiling allows,
        // which is what the read half needs and what no other server in this suite is cheap enough to do.
        put("row ceiling", "ok")
        // Where a statement may be written and what a delete has to carry. The expectations are the measured
        // ones: on the generic `"JDBC"` type a delete is refused by the dialect whether or not the script
        // wrote a limit, while a statement inside an `if` runs and counts its row. The update case is here to
        // say whether that refusal is about deletes or about every write with an optional limit.
        put("delete unlimited", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
        put("delete filtered", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
        put("delete limited", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
        put("update unlimited", "error=Limited update is not supported by this JDBC dialect. rowsAffected=<none>")
        put("statement in if", "error=<none> rowsAffected=1")
        put("setup in if", "error=<none> rowsAffected=1")
        // A nested loop's own value, with nothing in the body that waits. The suffixes count from the
        // outermost loop inward: `-1` is the outer loop and `-2` the inner, which the two cases read in
        // opposite directions so the rule cannot hold by accident.
        put("nested loop suffixed", "last=outer=2 inner=3")
        put("nested loop values", "last=outer=3 inner=2")
        // The stress cases. The three write cases are one experiment over the 30 000-value budget: 12 000 rows of
        // one column and 4000 rows of six are inside it, 12 000 rows of six are not and are sent as several
        // statements. All three must store every row, and the count each case reads back out of the table is
        // asserted by index — `size of` over the same result reported 0 for a page that held 5000 rows.
        put("stress batch", "error=<none> valuesAsked=12000 rowsReported=12000 rowsPaged=12000")
        put("stress columns", "error=<none> valuesAsked=24000 rowsReported=4000 rowsPaged=4000")
        put("stress split", "error=<none> valuesAsked=72000 rowsReported=12000 rowsPaged=12000")
        put("write values", "ok")
        put("local work", "ok")
        put("local results", "ok")
        // The declared line is the file backend's verdict, and only that. The same case failed on MySQL,
        // PostgreSQL and MongoDB with `rowsAffected=-1` in the run that first carried it (d0d86a6), and those
        // three are expected to keep failing it until the cancellation path is fixed: the case asks whether the
        // row the timed-out transaction inserted is still there, whether the connection can take a write
        // afterwards, and whether the key is free, and it prints all three answers. Do not relax this to a
        // prefix or a pattern to make those jobs green — the divergence is the finding.
        put(
            "stress cancel",
            "error=The database transaction was open for longer than 5 seconds and was rolled back. " +
                "inserted=1 found=0 readError=<none> fresh=1 freshError=<none> rowsAffected=1 sameError=<none>"
        )
        // Why `size of` answers zero for a stored result, with the controls that separate Skript's size query
        // from the plugin's storage: a flat list the script built reports its 20 values and loops 20 times, the
        // plugin's result and the same shape built by the script both report 0 and loop 0 times, and walking the
        // result's indices still counts 20 rows.
        // A range spelling such as `loop 1 to 20` is deliberately not part of it: Skript has no such loop, drops
        // the line, and the unparsed check above fails the build on it before this line is compared — which is
        // how that lead was settled. The case asserts the forms that do exist, with a control that reads the rows
        // without any loop at all.
        put(
            "result size",
            "built=20 flatLooped=20 mirror=0 mirrorLooped=0 read=0 readLooped=0 walked=20 local=0 copy=0 " +
                "fast=0 firstId=1 lastId=20 fastLastId=5000"
        )
        if (serverTestUsesDatabase) {
            putAll(serverTestDatabaseChecks)
            // A run whose implementation has no transactions reports the same lines as any other database
            // run, except for the transaction ones — so those are overridden, not the whole map. Replacing it
            // is what the first version of this did, and the MongoDB job's annotations said so within a minute
            // of the run: `setup`, `roundtrip now` and the connection lines had all been reported and were
            // "not known to this check".
            if (!serverTestImplementation.transactions) {
                putAll(serverTestNoTransactionChecks)
            }
        }
    }
}
