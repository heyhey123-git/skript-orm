# 表

**简体中文** | [English](tables.md)

表只描述一次，写在建立连接的那个脚本里；这份描述决定了插件如何拼语句、以及结果变量的键叫什么。

## 注册

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

主体是一行一列，形式固定为

```
name: type[(size)][, primary key][, auto increment][, not null]
```

- **列名**可以包含字母、数字、组合标记和下划线，不能以数字开头。`register a database table` 后面的**表名**守同一条规则，而且是语句执行时才检查：`"my table"`、`"2fa_codes"` 会被拒绝，报 `Invalid table name '…'` 落进 `last database error`，加不加引号都一样；列名则在解析脚本时就查，所以更早失败。名字按原样使用，拼写要保持一致：在 Linux 上运行的 MySQL，表名区分大小写。
- **类型**用 [类型](types.zh-CN.md) 里的名字。写错时，语句执行时会去当前连接的数据库里查这个类型，失败紧接着出现在 `last database error` 里，信息说明这个数据库不支持它。
- **括号里的长度**是给 `string` 的，且必须大于 0；不写就用该类型的默认值 255。写到别的类型上，PostgreSQL 会拒绝——那里 `uuid` 与 `location` 都是 `BYTEA`，不接受长度；MySQL 与 `"JDBC"` 则接受，并把它当成列的宽度：`location(16)` 比一个序列化后的 location 还小，之后每次写入都会因"数据过长"失败。不管括号写什么，`uuid` 就是 16 字节、`location` 就是 2048，所以长度留给 `string` 就好。
- **修饰符**与类型之间、以及彼此之间都用逗号分隔，可取 `primary key`、`auto increment`、`not null`、`nullable`。
  不写 `not null` 的列就是可空的；两者同时写会报错。

列必须是直接的行。嵌套块会被拒绝；重复列名、多个 `primary key`、以及给非主键列写 `auto increment` 同样会被拒绝。

## 主键有什么用

`primary key` 标出 `by id` 系列操作使用的那一列，分页也需要它：

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
update one entity in table "users" by id {_id} and wait
delete one entity from table "users" by id {_id} and wait
select page 2 with size 20 from table "users" and store the results in {_page::*}
```

`auto increment` 让数据库分配这个值。插件**不会**把生成的 id 交回脚本，所以需要知道它的脚本应该自己写一个值并用
`upsert`，或者事后按别的列把那一行找回来；见 [菜谱](cookbook.zh-CN.md)。

## 注册做了什么、没做什么

注册执行 `CREATE TABLE IF NOT EXISTS`，然后等到表存在为止。它不会删除、修改或检查任何东西。

**已经存在的表会被原样保留。** 在脚本里加一列再 reload，数据库不会有任何变化：插件记住了新列，数据库没有多出来，于是提到这一列的操作会在运行时报错，没提到的照常工作。这里**不会**有警告，因为从插件的角度看，注册确实成功了。要改表就自己执行 `ALTER TABLE`，开发库里也可以把表删掉让插件重建，亡羊补牢为时未晚。

**“已注册”是按连接记的。** 在同一个连接上再执行一次 `register a database table "users"`，会得到 `Table 'users' is already registered.`——常见的是第二个脚本往第一个脚本建好的那条连接上重名注册，或者 reload 之后没重新连接就注册。下面这一对语句每次都新建连接，所以避开了它：

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

`create a connection` 每执行一次就新建一条连接，而新连接里一张表都没注册，所以"先连接、再注册"的脚本 reload 之后照常能跑：reload 替换的是连接，不是往旧连接里再注册一次。真正会被拒的，是撞上一条还被别的东西持有着的连接。见 [连接](connections.zh-CN.md)。

## 失败

`register a database table` 没有 `and wait`：它一定会等，失败会紧接着暴露在 `last database error` 里：

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
if last database error is set:
    send "建表失败: %last database error%" to console
```

在没有 SkBee 的服务器上，`nbtcompound` 列会在这里就被拒绝，而不是等到第一行数据才失败；见 [类型](types.zh-CN.md)。
