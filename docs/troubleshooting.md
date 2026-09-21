# Troubleshooting

[简体中文](troubleshooting.zh-CN.md) | **English**

Some problems are easy to miss: an operation has no effect, a declared column cannot be read, or a table was never created. Each entry below explains the symptom, its cause, and what to do next.

## "I added a column and nothing changed"

Tables are created with `CREATE TABLE IF NOT EXISTS`. Registration never alters an existing table: the new column is added to the plugin's definition, but not to the database, and no warning is issued.

Operations that use the new column will then fail. Reading the table may also fail because the database result has no such column.

Migrate the database yourself:

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

If the data in a development database is disposable, you can drop the table and let the plugin recreate it. For a live database, use your existing migration tools; the plugin does not handle migrations. See [Tables](tables.md).

## "Table 'users' is already registered."

Table registrations belong to a **connection**, and `create a connection` creates a new connection each time. This error means the same table name was registered twice on one connection, perhaps by a second script or after a reload without reconnecting. Connecting and registering together in `on load` replaces the connection on reload and avoids this kind of duplicate registration:

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
```

See [Tables](tables.md).

## "Table 'users' not found."

The table has not been registered **on the current connection**. The script responsible for connecting and registering may not have run, or it may have failed earlier; check `last database error` at the point of failure. Another possibility is that the connection was replaced, leaving the new connection with no registered tables.

## "No database connected."

There is no active connection: none was created, the connection attempt failed, or the connection was closed. A failed connection attempt also leaves the previous connection closed, so incorrect credentials can affect every script sharing that connection. See [Connections](connections.md).

If named connections exist but none is the default, the error says so and lists their names. Use `use connection "logs"` or `make connection "logs" the default`; there is no need to create another connection.

## A column reads as unset, and the row exists

Skript deletes list-variable keys whose values are null, so a NULL column has no key in the result. `{_user::age} is not set` can mean "age is NULL" or "no row", not "age is zero". Check a column that cannot be NULL to distinguish the two:

```sk
if {_user::id} is not set:
    send "No such row."
else if {_user::age} is not set:
    send "The row exists, the age is NULL."
```

See [Types](types.md).

## `{_users::name}` is empty after `select many`

`select many` results start with a one-based row index, as in `{_users::1::name}`. Even a single matching row needs that index. There is no row-count expression for these results: `size of {_users::*}` counts first-layer values, but each row is a sub-list. Walk the row indices or keep your own counter. See [Reading rows](reading.md).

## A delete or update touched every row

`delete entities` and `update entities` allow you to omit `where`, in which case they affect every row the implementation allows. Forgetting the condition is not an error. Add a filter or use `by id`. See [Updating and deleting](updating-and-deleting.md).

## "Data type 'nbtcompound' cannot be used: SkBee is not installed, and it is what provides NBT compounds."

`nbtcompound` requires SkBee. Without it, table registration fails rather than waiting until the first write. Install SkBee or choose another type. Scripts also need SkBee to construct NBT compounds; servers that do not use this type are unaffected. See [Types](types.md).

## "Auto-increment column 'id' must also be a primary key."

`auto increment` requires `primary key` on the same column because the value must identify the row:

```sk
    id: bigint, primary key, auto increment, not null
```

## The id after an insert is unknown

The plugin does not return generated ids. Either assign the id in your script and use `upsert`, or find the inserted row by another column. See [Cookbook](cookbook.md).

## A `date` column lost its time

On SQL implementations, a `date` column stores SQL `DATE`, which discards the time of day. For an exact moment, store a Unix timestamp in a `bigint`, with a consistent choice of seconds or milliseconds, or use a `string` containing a timestamp with a time zone. `timespan` represents a duration, not a point in time. MongoDB's `date` column keeps epoch milliseconds instead. A `time` column represents a Minecraft time of day, not a wall-clock reading. See [Types](types.md).

## Paging skips or repeats rows

A page is an offset into primary-key order, not a snapshot. Inserts or deletes during pagination can shift later rows, causing duplicates or omissions. A keyset filter (`id > {_last}`) requires a stable primary-key sort order. `select many` does not offer `ORDER BY`, so adding that filter alone is not a complete replacement for `select page`.

Pagination sorts by the registered primary key and rejects tables without one. Page numbers start at 1, and row indices restart at 1 within each page: `{_page::1::name}` is the first row on that page, not the first row in the table. See [Reading rows](reading.md).

## Skript says "Empty configuration section!"

Skript warns when a section has no indented content beneath its colon, regardless of which plugin provides it. The message comes from Skript's parser, and its control flag is internal; neither a config file nor a script can disable it.

Forms that take values from a variable or operate by id need no body, so they use no colon. A line in this form is an effect, not a section, and does not trigger the empty-section warning:

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

Use a colon only when the statement has an indented body. To read a single row, you can supply a filter that covers the rows you want; this example suits a table whose ids start at 1:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        id >= 1
```

Sections with a `where` block, `values` block, or other body need no changes. Older scripts with empty sections still run but still warn. For the one-line forms above, removing the colon resolves the warning.

## The table name works on one server and not another

MySQL table names on Linux are usually case-sensitive, depending on the server configuration. Both `register a database table "..."` and every later `table "..."` use the name exactly as written. Keep the spelling and case consistent.
