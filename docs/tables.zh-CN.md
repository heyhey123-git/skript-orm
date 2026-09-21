# 表

**简体中文** | [English](tables.md)

在建立连接的脚本中定义表结构，插件会据此生成语句，并确定结果变量中的键名。

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
- **类型**必须使用 [类型](types.zh-CN.md) 中列出的名称。不支持的类型会在 section 执行时被拒绝，紧接着可在 `last database error` 中读取错误信息。
- **括号中的长度**用于 `string`，必须大于 0，省略时默认为 255。为其他类型指定长度时，PostgreSQL 会拒绝，因为其中的 `uuid` 和 `location` 使用不接受长度参数的 `BYTEA`；MySQL 和 `"JDBC"` 则会将其作为列宽。例如，`location(16)` 无法容纳序列化后的位置数据，写入会因“数据过长”而失败。`uuid` 的数据长度为 16 字节，`location` 为 2048 字节，不随括号中的值改变，因此只为 `string` 指定长度。
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

## 注册做了什么、没做什么

在 SQL 后端，注册会执行 `CREATE TABLE IF NOT EXISTS`，并等待完成。它不会删除、修改或检查已有表结构。

**已有表会原样保留。** 在脚本中新增列后重新加载，数据库中的表结构不会改变。插件虽然记录了新列，但数据库中没有对应列，引用它的操作会在运行时报错，其他操作仍可正常执行。此时**不会**出现注册警告，因为注册本身已经成功。要修改表结构，请自行执行 `ALTER TABLE`；在开发数据库中，也可以删除表后由插件重新创建。

**注册信息按连接保存。** 在同一连接上再次执行 `register a database table "users"`，会报 `Table 'users' is already registered.`。常见原因包括多个脚本在共享连接上注册同名表，或重新加载脚本后未重建连接便再次注册。下面的写法每次都先建立新连接，可以避免重复注册：

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

`create a connection` 每次都会创建新连接，初始状态下没有已注册的表。因此，“先连接、再注册”的脚本可以重新加载：它替换连接，而不是向旧连接重复注册。真正会被拒绝的是在同一连接上再次注册同名表。见 [连接](connections.zh-CN.md)。

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
