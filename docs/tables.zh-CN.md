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

在 SQL 后端，注册会执行 `CREATE TABLE IF NOT EXISTS` 并等待完成，然后把表从服务端读回来，与声明逐项比较。

**已有表会原样保留。** 在脚本中新增列后重新加载，数据库中的表结构不会改变——插件只更新自己的定义。注册本身是成功的，因此不会给出警告；也正是 `CREATE TABLE IF NOT EXISTS` 让「每次启动都注册同一张表」是安全的。

**表支撑不了声明时，注册当场失败。** 由于上面那张表永远不会被修改，如果脚本给一张已存在的表加上 `age: int`，本来会拖到第一条用到该列的语句才报服务端的 `Unknown column 'age' in 'INSERT INTO'`——那条消息指向写入，而不是注册。现在 `register a database table` 会直接失败，`last database error` 会把差异说清楚：

```
Registered table 'users' does not match the table in the database. Registration is 'CREATE TABLE IF
NOT EXISTS', so a table that already exists is never changed, and every statement that uses the column
or type below will fail.
Column(s) 'age' are declared but missing from the table. The table holds: 'id', 'name'.
Drop the table and register it again, or change the table in the database to match this declaration.
```

比较只问一件事：这张表能不能支撑声明允许的每一条语句。所以它要求声明的每一列都在、类型对得上且长度够用——更宽的列装得下更窄声明写出的值，反过来则不行；要求非主键列上的 `not null` 成立，并要求表能保证声明的键：如果表的主键里有一列声明没有标成主键，就会被拒绝——因为用声明的键定位一行的语句此时可能命中多行。没有声明键的声明没有对身份做任何承诺，所以表本身带主键也不构成差异。`uuid` 是长度上的例外：它的 `BINARY(16)` 是固定宽度而不是容量，更宽的列读回来会带补白，所以表里必须正好是这个宽度。

**不比较** `auto increment`（两种服务端通过不同的元数据报告它），也不比较表里多出来、声明从未提过的列：那不是差异——与别的工具共用一张表，或旧声明创建的表多留了几列，都仍然能跑通声明写出的每条语句。唯一的例外是这类列若为 `not null` 且没有默认值：省掉它去插入会被服务端拒绝，而那条消息会点出列名。要改表结构，请自行执行 `ALTER TABLE`；在开发数据库中也可以删除表后由插件重新创建。

**注册信息按连接保存。** 在已持有该表的连接上再次执行 `register a database table "users"`，不会报错也不会改动已有结构：声明被接受，已注册的结构保持不变。这正是"在 `on load` 里声明表"的脚本可以安全重载的原因——它连接到的连接上表已经注册，重复声明是预期结果而非错误。

要改表结构，请重新建立连接（让注册重新创建），或删除表后由插件重新创建。用不同的列重复声明**不会**改动已有的表，所以改动过声明的表仍保留最初创建时的结构：

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

`create a connection` 每次都会创建新连接，初始状态下没有已注册的表。因此“先连接、再注册”的脚本注册到的是刚建立的连接；而只做注册（连接到已持有这些表的连接）则是无操作，而不是错误。见 [连接](connections.zh-CN.md)。

不由注册信息构建的语句就是[原始语句](raw-statements.zh-CN.md)。它按原样发出，因此**不会**与它指名的表做任何比对——不比列、不比类型、也不检查表是否存在。本页讲的全是声明式写法，也就是会被检查的那一种。

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
