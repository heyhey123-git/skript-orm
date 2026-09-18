# Types

[简体中文](types.zh-CN.md) | **English**

The type of a column is written in the table definition, and it decides what a script may put in and
what comes back out.

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
    joined: date, nullable
```

## The type names

| Type | In a script | Stored as | Size |
| --- | --- | --- | --- |
| `boolean` | `true` / `false` | `BOOLEAN` | |
| `tinyint` | a whole number that fits in a byte (−128…127) | `TINYINT` | |
| `int` | a whole number | `INT` | |
| `bigint` | a whole number, including values past what `int` holds | `BIGINT` | |
| `double` | a fractional number | `DOUBLE` | |
| `float` | a fractional number | `FLOAT` | |
| `string` | text | `VARCHAR` | `string(64)`, default 255 |
| `uuid` | a UUID | `BINARY(16)` | fixed 16 |
| `itemstack` | an item stack, with its meta and NBT | `BLOB` | |
| `location` | a location, including its world | `VARBINARY` | default 2048 |
| `bukkitserializable` | anything Bukkit can serialize | `BLOB` | |
| `nbtcompound` | an NBT compound (needs SkBee) | `BLOB` | |
| `date` | a Skript date | `DATE` | |
| `time` | a Skript time | `INT` | |
| `timespan` | a Skript timespan | `BIGINT` | |

A size in brackets is only meaningful for the types that have one: `string`, `uuid` and `location`.
`uuid` and `location` have defaults that are already right for their contents, so a size is usually
only written for `string`.

### On MongoDB

The "Stored as" column above is the SQL type, and it is what `"MySQL"`, `"JDBC"` and `"PostgreSQL"`
create. `"MongoDB"` is the type name with no SQL under it: a document holds BSON, nothing becomes a SQL
column, and the same logical types map like this. The rest of what that implementation does differently
is on [Compatibility](compatibility.md#mongodb).

| Type | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32 — the byte widened to a whole number |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double — the float widened |
| `string` | string |
| `uuid` | binary — the 16 bytes of its two halves |
| `itemstack` | binary — the same serialized payload the SQL implementations store in a `BLOB` |
| `location` | binary — the same serialized payload the SQL implementations store in a `VARBINARY` |
| `bukkitserializable` | binary — the same serialized payload the SQL implementations store in a `BLOB` |
| `nbtcompound` | binary — the same serialized payload the SQL implementations store in a `BLOB` |
| `date` | int64 — the epoch milliseconds |
| `time` | int32 — the ticks (0…24000) |
| `timespan` | int64 — the milliseconds |

`size` and `nullable` are declarations only here: a collection has no schema, and nothing enforces them.
What registration actually creates is the unique index on the primary key and the counter behind `auto
increment`, and MongoDB creates the collection itself with the first document written.

**A `date` column keeps the exact moment on MongoDB.** BSON has no date-only type, so the column is the
epoch milliseconds above and the time of day is *not* dropped. It is the one type whose meaning differs
between the implementations; everything the statements do differently is on
[Compatibility](compatibility.md#mongodb). The next section describes the SQL implementations.

## Two ways a value can surprise you

**A `date` column keeps the day, not the time — on the SQL implementations.** There it is stored as a
SQL `DATE`, so the hour, minute and second are dropped, and a script that needs the exact moment should
store a `bigint` of epoch milliseconds or a `timespan` instead. MongoDB is the exception, for the reason
[above](#on-mongodb).

**A `time` column is a Minecraft time of day**, the same number Skript's `time` type carries (0…24000),
not a wall clock reading. Use `date` for a calendar value and `timespan` for a duration.

## NULL

SQL NULL is reachable, and it has two separate rules:

- **Writing it.** In a `values` block, a literal `null` stores SQL NULL. Leaving the column out of the
  block is not the same thing: a column that is left out is not part of the statement. An `insert`
  leaves it to the database default, which for a column defined without `not null` is NULL and for an
  auto-increment key is the next id, while an `update` or an `upsert by id` leaves it at its stored
  value.
- **Reading it.** A NULL column leaves its key unset in the result variable. In a list variable Skript
  deletes a key whose value is set to null, so a NULL column and a column that does not exist look the
  same from a script. `{_user::age} is not set` therefore means "NULL or absent", not "zero".

## NBT compounds

`nbtcompound` is the one type whose implementation lives in another plugin: SkBee is what gives scripts
a way to build a compound, and the plugin reads and writes compounds through SkBee's classes.

- **Without SkBee**, registering a table that declares an `nbtcompound` column is refused, with a
  message naming SkBee. The rest of the plugin works normally on such a server; NBT is the only part
  that needs it.
- **The stored form is NBT**, not text: the compound is written as NBT bytes into a `BLOB`, so what
  comes back is a compound rather than a string that has to be parsed.
- **The value is a snapshot, taken when the script names it.** A compound SkBee hands over for an item,
  entity or block is a view over that live object: changing the compound changes the object. What the
  database stores is the compound as it was at the moment the operation was written, so a later change
  to the item does not rewrite history.
- **Comparing is easiest through text.** SkBee renders a compound as SNBT, so a script can check what
  came back with `"%{_row::data}%" contains "someTag"` instead of walking the compound.

## What a mismatch looks like

A value the column cannot hold fails the operation rather than being stored approximately. With
`and wait`, `last database error` says which column and what was expected; without it, the failure is
only logged. See [Errors and waiting](errors-and-waiting.md).
