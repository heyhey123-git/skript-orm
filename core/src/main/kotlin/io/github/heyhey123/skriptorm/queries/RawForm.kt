package io.github.heyhey123.skriptorm.queries

/**
 * The shape of a backend's own raw entry point.
 *
 * This is what lets the addon stay out of the question "is this connection SQL or not": nothing in the
 * core enumerates backends or decides which of them can take a raw statement. An implementation declares
 * the one shape it has, overrides the raw methods it can serve, and inherits refusals for the rest — and a
 * refused statement can then name the alternative that backend does take.
 *
 * A backend with no raw entry point at all declares null.
 */
enum class RawForm {

    /**
     * Statements are text sent to a relational server, with `?` placeholders bound to parameters.
     *
     * The statements a script writes are the server's own, so what is accepted is what that server accepts;
     * the addon does not translate between dialects.
     */
    SQL_STATEMENT,

    /**
     * Statements are documents sent to a document server as one command, answered with one document.
     *
     * There is no separate count of affected rows and no result set: whatever the server changed, it
     * reports inside the document it answers with.
     */
    COMMAND_DOCUMENT
}
