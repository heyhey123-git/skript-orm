# 表

**简体中文** | [English](tables.md)

建立连接后，请在需要使用该表的连接上注册表结构。插件会根据表定义生成语句，并确定结果变量中的键名。

## 注册

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

主体每行定义一列，格式如下：

```
name: type[(size)][, primary key][, auto increment][, not null]
```

- **列名**可以包含字母、数字、组合标记和下划线，但不能以数字开头。`register a database table` 后的**表名**遵循同样的规则，在语句执行时检查。`"my table"`、`"2fa_codes"` 等名称无论是否加引号，都会被拒绝，`last database error` 中会记录 `Invalid table name '…'`。列名则在脚本解析时检查，因此会更早报错。名称按原样使用，请保持拼写一致；Linux 上的 MySQL 表名区分大小写。
- **类型**必须使用 [类型](types.zh-CN.md) 中列出的名称。不支持的类型会在注册表语句执行时被拒绝，紧接着可在 `last database error` 中读取错误信息。
- **括号中的长度**用于 `string`，必须大于 0，省略时默认为 255。为其他类型指定长度时，PostgreSQL 会拒绝，因为其中的 `uuid` 和 `location` 使用不接受长度参数的 `BYTEA`；MySQL 和 `"JDBC"` 则会将其作为数据库列的长度。例如，`location(16)` 无法容纳序列化后的位置数据，写入会因“数据过长”而失败。`uuid` 的数据长度为 16 字节，`location` 为 2048 字节，不随括号中的值改变，因此只为 `string` 指定长度。
- **修饰符**与类型之间、各修饰符之间均用逗号分隔，可用 `primary key`、`auto increment`、`not null` 和 `nullable`。
  未声明 `not null` 的列默认可空；同时声明 `not null` 和 `nullable` 会报错。

列必须直接写在主体中，不能嵌套成块。重复列名、多个 `primary key`，以及非主键列上的 `auto increment` 也会被拒绝。

## 主键有什么用

`primary key` 指定 `by id` 操作使用的列，分页也需要主键：

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
update one entity in table "users" by id {_id} and wait
delete one entity from table "users" by id {_id} and wait
select page 2 with size 20 from table "users" and store the results in {_page::*}
```

`auto increment` 由数据库分配值。插件**不会**将生成的 id 返回给脚本；如果后续需要使用它，可以自行指定值并使用 `upsert`，或通过其他列重新查询该行。见 [菜谱](cookbook.zh-CN.md)。

## 注册表时会发生什么

在 SQL 后端，注册会执行 `CREATE TABLE IF NOT EXISTS` 并等待完成，然后把表从服务端读回来，与声明逐项比较。

**已有表不会被修改。** 在脚本中新增列并重新加载，不会为数据库中的表添加列。重复注册相同的定义会直接成功；修改后的定义会与现有表结构比较，不兼容时会报错。

**声明与现有表不兼容时，注册立即失败。** 例如，脚本声明了 `age: int`，但数据库中的表没有 `age` 列，`last database error` 会指出具体差异：

```
Registered table 'users' does not match the table in the database. Registration is 'CREATE TABLE IF
NOT EXISTS', so a table that already exists is never changed, and every statement that uses the column
or type below will fail.
Column(s) 'age' are declared but missing from the table. The table holds: 'id', 'name'.
Drop the table and register it again, or change the table in the database to match this declaration.
```

检查要求声明中的每一列都存在，类型兼容、容量足够；还会检查非主键列的 `not null` 约束，以及数据库是否能保证声明中的主键。如果声明没有主键，数据库中的表仍可有主键。`uuid` 是长度上的例外：其 `BINARY(16)` 宽度必须完全一致，更宽的列读出时会带补白。

检查不比较 `auto increment`，因为不同数据库的元数据表示不一致；也允许数据库中存在声明未提到的额外列。不过，如果额外列有 `not null` 约束且没有默认值，省略该列的插入仍会失败。修改表结构时，请自行执行 `ALTER TABLE`；在开发数据库中也可以删除表，再由插件创建。

**注册信息按连接保存。** 在同一连接上重复注册相同定义，会直接成功，不再检查数据库。因此，在 `on load` 中注册表的脚本可以安全重载。

修改表结构需要直接操作数据库，或在开发环境中删除表后由插件重新创建。重新连接可以刷新连接中的注册信息，但不会修改数据库中已有的表。例如：

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

`create a connection` 建立的新连接尚未注册任何表。示例中的 `register a database table "users"`
会在该连接上注册 `users`；重新连接后需要再次运行注册语句。如果连接已有完全相同的定义，重复注册也可以。详见[连接](connections.zh-CN.md)。

[原始语句](raw-statements.zh-CN.md)不会执行表注册和结构核对。插件按原样发送语句，不会根据注册信息核对表名或列定义。

## 失败

`register a database table` 不接受 `and wait`，因为它始终等待完成。操作失败后，可立即读取 `last database error`：

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
if last database error is set:
    send "建表失败: %last database error%" to console
```

未安装 SkBee 时，包含 `nbtcompound` 列的表会在注册时被拒绝，不会等到读写数据时才报错。见 [类型](types.zh-CN.md)。
