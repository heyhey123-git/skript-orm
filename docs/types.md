# Types

[简体中文](types.zh-CN.md) | **English**

A column's type is declared in the table definition. It determines what a script can write and what it gets back.

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

Use parentheses to set a `string` length. `uuid` always takes 16 bytes, and `location` has its own serialized format. PostgreSQL does not accept a size for either of those types; see [Tables](tables.md).

### On MongoDB

The "Stored as" column above applies to `"MySQL"` and `"JDBC"`. `"PostgreSQL"` differs slightly: `tinyint` maps to `SMALLINT`, `float` to `REAL`, and `double` to `DOUBLE PRECISION`. All binary types (`uuid`, `itemstack`, `location`, `bukkitserializable`, `nbtcompound`) map to `BYTEA`, which takes no size, so `uuid(16)` and `location(2048)` are rejected.

`"MongoDB"` stores BSON documents rather than SQL columns. The same logical types map to BSON as follows; other differences are covered in [Compatibility](compatibility.md#mongodb).

| Type | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32 (widened from a byte) |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double (widened from a float) |
| `string` | string |
| `uuid` | binary (the 16 bytes of its two halves) |
| `itemstack` | binary (the same serialized payload SQL implementations store in a `BLOB`) |
| `location` | binary (the same serialized payload SQL implementations store in a `VARBINARY`) |
| `bukkitserializable` | binary (the same serialized payload SQL implementations store in a `BLOB`) |
| `nbtcompound` | binary (the same serialized payload SQL implementations store in a `BLOB`) |
| `date` | int64 (epoch milliseconds) |
| `time` | int32 (ticks, 0…24000) |
| `timespan` | int64 (milliseconds) |

`size` and `nullable` are declarations only here; the collection has no schema enforcing them. Registration creates the primary-key index and prepares the `auto increment` counter as required by the table definition. Creating an index may also create the collection; if it does not yet exist, the first document write creates it.

**On MongoDB, a `date` column keeps the exact moment.** BSON has no date-only type, so this column stores epoch milliseconds and does **not** discard the time of day. This is the only type whose meaning differs between implementations. Statement-level differences are covered in [Compatibility](compatibility.md#mongodb); the next section describes SQL behavior.

## Two ways a value can surprise you

**On SQL implementations, a `date` column keeps the date, not the time.** It is stored as SQL `DATE`, so hours, minutes, and seconds are discarded. For an exact moment, store a Unix timestamp in a `bigint`, with a consistent choice of seconds or milliseconds, or use a `string` containing a timestamp with a time zone. `timespan` represents a duration, not a point in time. MongoDB is the exception, as explained [above](#on-mongodb).

**A `time` column is a Minecraft time of day**, the number carried by Skript's `time` type (0…24000), not a wall-clock reading. Use `date` for a calendar value and `timespan` for a duration.

## NULL

Scripts can read and write SQL NULL, with a separate rule for each direction:

- **Writing it.** A literal `null` in a `values` block stores SQL NULL. Omitting a column is different: it leaves the column out of the statement. An `insert` uses the database default, which is NULL for a column declared without `not null`, or the next id for an auto-increment key. An `update` or `upsert by id` preserves omitted columns in an existing row.
- **Reading it.** A NULL column has no key in the result variable. Skript deletes list-variable keys whose values are null, so a script cannot distinguish a NULL column from an absent column by that key alone. `{_user::age} is not set` means "NULL or absent", not "zero".

## NBT compounds

`nbtcompound` is the only type implemented through another plugin. SkBee provides the syntax for building compounds, and this plugin uses SkBee's classes to read and write them.

- **Without SkBee**, registering a table with an `nbtcompound` column fails with an error that names SkBee. The rest of the plugin works normally.
- **The stored form is NBT, not text.** The compound is written as NBT bytes into a `BLOB` and read back as a compound, with no string parsing needed.
- **The stored value is a snapshot taken when the script supplies it.** A compound SkBee returns for an item, entity, or block is a live view: changing the compound changes the object. The database stores its contents when the operation captures the value; later changes to the item do not affect the saved data.
- **Text is a convenient way to check the contents.** SkBee renders compounds as SNBT, so a script can use `"%{_row::data}%" contains "someTag"` to inspect a result without walking the compound.

## What a mismatch looks like

A value that does not fit the column type fails the operation. `last database error` identifies the column and expected type. Range checks and precision loss need to be considered separately:

- **Integer range checks happen before the statement is sent.** `tinyint` holds −128…127, `int` holds −2147483648…2147483647, and `bigint` holds 64-bit integers. Out-of-range values produce an error rather than being cut down to fit. **Fractions written to integer columns are still truncated toward zero**: writing `1.7` to an `int` column stores `1`, consistent with Skript's integer conversion.
- **`float` uses four bytes; `double` can represent integers exactly up to 2^53.** Writing `0.1` to a `float` column returns `0.10000000149011612`, and writing a `bigint`-sized number to a `double` column can lose low bits. These are precision limits of the types, not errors the plugin can prevent.

See [Errors and waiting](errors-and-waiting.md).
