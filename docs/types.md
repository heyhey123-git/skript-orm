# Types

[简体中文](types.zh-CN.md) | **English**

Declare column types when registering a table. The type determines which values you can write and what you read back.

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
    joined: date, nullable
```

## The type names

| Type | Script value | MySQL/JDBC storage | Size |
| --- | --- | --- | --- |
| `boolean` | `true` / `false` | `BOOLEAN` | |
| `tinyint` | a one-byte integer (−128…127) | `TINYINT` | |
| `int` | an integer | `INT` | |
| `bigint` | an integer, including values beyond the `int` range | `BIGINT` | |
| `double` | a fractional number | `DOUBLE` | |
| `float` | a fractional number | `FLOAT` | |
| `string` | text | `VARCHAR` | `string(64)`, default 255 |
| `uuid` | a UUID | `BINARY(16)` | fixed 16 |
| `itemstack` | an item stack, including its meta and NBT | `BLOB` | |
| `location` | a location, including its world | `VARBINARY` | default 2048 |
| `bukkitserializable` | anything Bukkit can serialize | `BLOB` | |
| `nbtcompound` | an NBT compound (requires SkBee) | `BLOB` | |
| `date` | a Skript date | `DATE` | |
| `time` | a Skript time | `INT` | |
| `timespan` | a Skript timespan | `BIGINT` | |

Specify a `string` length in parentheses, as in `string(64)`. UUIDs always occupy 16 bytes. Locations use a serialized format and default to 2048 bytes. PostgreSQL does not accept a size for `uuid` or `location`; see [Tables](tables.md).

### PostgreSQL and MongoDB

The storage column above applies to `"MySQL"` and `"JDBC"`. PostgreSQL uses `SMALLINT` for `tinyint`, `REAL` for `float`, and `DOUBLE PRECISION` for `double`. It stores binary types (`uuid`, `itemstack`, `location`, `bukkitserializable`, `nbtcompound`) as `BYTEA`, which does not take a size. Declarations such as `uuid(16)` and `location(2048)` are therefore rejected.

MongoDB stores BSON documents rather than SQL rows. Its type mappings are below; see [Compatibility](compatibility.md#mongodb) for differences in statement behavior.

| Type | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32 (widened from a byte) |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double (widened from a float) |
| `string` | string |
| `uuid` | binary (16 bytes) |
| `itemstack` | binary (the same serialized data used by SQL implementations) |
| `location` | binary (the same serialized data used by SQL implementations) |
| `bukkitserializable` | binary (the same serialized data used by SQL implementations) |
| `nbtcompound` | binary (the same serialized data used by SQL implementations) |
| `date` | int64 (epoch milliseconds) |
| `time` | int32 (ticks, 0…24000) |
| `timespan` | int64 (milliseconds) |

MongoDB does not enforce the declared size or nullability of a column. Registering a table creates its primary-key index and, when needed, an `auto increment` counter. The collection may be created with the index or on the first write.

**MongoDB `date` columns preserve the time of day** as epoch milliseconds. SQL `date` columns behave differently, as explained below.

## Dates and time

**SQL `date` columns keep only the calendar date.** Because they use SQL `DATE`, they discard hours, minutes, and seconds. To preserve an exact moment, use a `bigint` Unix timestamp (consistently in seconds or milliseconds) or a `string` containing a timestamp and time zone. MongoDB `date` columns preserve the time of day, as explained [above](#postgresql-and-mongodb).

**A `time` column holds Minecraft time of day:** Skript's `time` value, from 0 to 24000 ticks. It does not hold wall-clock time. `timespan` represents a duration.

## NULL

SQL NULL is handled differently when writing and reading:

- **Writing:** A literal `null` in a `values` block stores SQL NULL. Omitting the column instead uses its database default on insert. On update or `upsert by id`, omitted columns in an existing row keep their values.
- **Reading:** A NULL column has no key in the result variable because Skript removes list-variable keys with null values. `{_user::age} is not set` can therefore mean either NULL or an absent column; it does not mean zero.

## NBT compounds

`nbtcompound` is the only type implemented through another plugin. SkBee provides the syntax for building compounds, and this plugin uses SkBee's classes to read and write them.

- **Without SkBee**, registering a table with an `nbtcompound` column fails with an error that names SkBee. The rest of the plugin works normally.
- **The stored form is NBT, not text.** The compound is written as NBT bytes into a `BLOB` and read back as a compound, with no string parsing needed.
- **The stored value is a snapshot.** SkBee may return a live view of an item, entity, or block. The database saves the compound's contents when the operation takes its value; later changes to the source object do not change what was saved.
- **SkBee renders compounds as SNBT.** To inspect a result as text, use an expression such as `"%{_row::data}%" contains "someTag"`.

## What a mismatch looks like

When a value cannot be converted to its column type, the operation fails and `last database error` identifies the column and expected type. Numeric conversions have a few additional rules:

- **Integer range checks happen before the statement is sent.** `tinyint` holds −128…127, `int` holds −2147483648…2147483647, and `bigint` holds 64-bit integers. Out-of-range values produce an error rather than being cut down to fit. **Fractions written to integer columns are still truncated toward zero**: writing `1.7` to an `int` column stores `1`, consistent with Skript's integer conversion.
- **Numbers in `string` columns are stored as text.** `42` and `42.0` both become `"42"`; `1.5` becomes `"1.5"`. The plugin performs this conversion because Skript does not provide a number-to-text converter for these column values. Existing text is stored unchanged.
- **`float` uses four bytes; `double` can represent integers exactly up to 2^53.** Writing `0.1` to a `float` column returns `0.10000000149011612`, and writing a `bigint`-sized number to a `double` column can lose low bits. These are precision limits of the types, not errors the plugin can prevent.

See [Errors and waiting](errors-and-waiting.md).
