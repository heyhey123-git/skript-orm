package io.github.heyhey123.skriptorm.impl.mongo

/**
 * The field MongoDB reserves for the identifier it gives every document.
 *
 * This is not a column of a table but the identity of the document itself, so a table may not declare a
 * column with this name: the server refuses to index it a second time, and it refuses to let an update
 * write it. Statements that have to name a row of a collection without knowing the table's key — the
 * limited update and delete, which take whichever rows the filter matched — use it here.
 */
internal const val DOCUMENT_ID = "_id"
