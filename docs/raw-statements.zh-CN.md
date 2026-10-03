# 原始语句

**简体中文** | [English](raw-statements.md)

原始语句可以直接发送 SQL 或 MongoDB 命令，无需注册表。插件不会检查表名和列名、按声明的列类型转换值，也不会为其他数据库改写 SQL。连接账号有权执行的操作，原始语句都能执行，包括删表和删库。

迁移数据，或执行表操作语法无法表达的操作时，可以使用原始语句。如果表操作语句被意外拒绝，请报告问题，不要用原始 SQL 绕过检查。

服务端测试会解析[示例脚本](examples/raw-statements.sk)，检查其中的语法。

## 什么时候该用它

- **调整表结构或迁移数据：** `ALTER TABLE`、`CREATE INDEX`、修正已有数据。
- **数据库特有的 SQL：** 厂商扩展、优化器提示等。
- **表操作语法不支持的查询：** 多表连接、计算列、`GROUP BY`。
- **动态子句：** 在运行时构造 `WHERE` 或 `ORDER BY`。`where` 块不能把一个值当作列名。

表操作语句能完成的操作，尽量用表操作语句。它们会在发送查询前检查列并转换值。

## SQL 语句

查询结果用 `execute query` 读取，受影响行数用 `execute update` 获取：

```sk
execute query "SELECT id, name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}
execute update "ALTER TABLE users ADD COLUMN age INT NULL"
set {_values::*} to 30, "Alice"
execute update "UPDATE users SET age = ? WHERE name = ?" with {_values::*} and store affected rows in {_rows}
```

- **`execute query`** 与 `select many` 一样，将结果行存为 `{_rows::1::name}`、`{_rows::2::name}`。键名取自服务端返回的列标签，例如 `SELECT name AS who` 对应 `{_rows::1::who}`。没有匹配的行时不存结果，也不算出错。
- **`execute update`** 可以存储受影响行数。服务端对某些语句（例如 DDL）可能返回 `0`，此时存下的也是 `0`。
- **两种写法都会等待**数据库响应，再执行下一行。加上 `and wait` 不会改变行为。

## MongoDB 命令

MongoDB 命令返回文档，不返回 SQL 结果行或受影响行数。因此要使用 `execute command`：

```sk
execute command "{ ""count"": ""users"" }" and store the result in {_answer::*}
send "现在有 %{_answer::n}% 个用户。"
```

命令写成 JSON 文本，由 MongoDB 驱动解析，也支持日期和对象 ID 的扩展 JSON 写法。结果中的字段按名称读取，数组元素按下标读取，例如 `{_answer::n}` 或 `{_answer::cursor::firstBatch::1::name}`。

`execute command` 不支持参数，需要提供完整的 JSON 命令。

## 参数

在要放值的地方写 `?`，然后在 `with` 子句里按同样顺序给出值：

```sk
execute query "SELECT name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}

set {_values::*} to 18, "zh-CN"
execute query "SELECT name FROM users WHERE age > ? AND language = ?" with {_values::*} and store the result in {_rows::*}
```

`with` 子句接受单个值或列表变量。传多个值时，先存入列表变量。`with (18, "zh-CN")` 无法通过语法解析，因为 Skript 不能在这里传入这样的字面量列表。

- 值的个数必须与语句里 `?` 的个数一致，不一致会在**发出之前**被拒绝。
- 参数值由驱动绑定，不会被解释为 SQL。传值时始终使用参数，不要把值拼进语句文本。
- 插件会统计每一个 `?`，包括 SQL 字符串和注释中的问号。避免在这些位置使用 `?`，否则插件和驱动对参数数量的判断可能不同。
- **参数必须是驱动能直接发送的值：** 字符串、数字、布尔值、字节数组、日期或 `null`。UUID、物品、坐标等对象会被拒绝，因为原始语句没有可供判断转换方式的列类型。传参前请自行转换。

## 检查与保障

| 行为 | 原始语句 |
| --- | --- |
| 未知列名在语句发出前失败 | **不成立。** 由服务端在收到时判断。 |
| 注册时核对声明与数据库中的表 | **不成立。** 原始语句不使用表声明。 |
| 值会按列的类型与范围检查 | **不成立。** 值按原样绑定。 |
| 实现不读的连接属性被拒绝 | **成立。** 那条检查与语句无关。 |
| 缺连接时在运行前报错 | **成立。** 语句被拒绝，不会被发出。 |
| 失败进入 `last database error`，且是服务端原文 | **成立**，包含服务端的错误码。 |
| 参数值不会被当作 SQL 执行 | **成立**，前提是使用 `?` 占位符。 |
| 语句在 `database transaction` 内运行并随事务回滚 | **成立。** 原始语句属于它所在的事务。 |

## 按连接类型选择语句

SQL 连接接受 `execute query` 和 `execute update`；MongoDB 连接接受 `execute command`。用错语句时，插件会在发送前报错，并提示正确的形式：

```sk
in connection "logs":          # 这是一条 MongoDB 连接
    execute query "SELECT 1" and store the result in {_rows::*}
```

```
Raw SQL statements are not supported by database 'MongoDB'. It takes raw commands instead:
'execute command "…"'. See the Raw statements page for what a raw statement does and does not guarantee.
```

## 账号权限

原始语句使用连接账号的权限运行。请只授予这个账号脚本所需的权限。详见[账号凭据](connections.zh-CN.md#账号凭据)。

## 参见

- [连接](connections.zh-CN.md) —— 原始语句以哪个账号运行。
- [表](tables.zh-CN.md) —— 表操作语句会执行哪些检查。
- [写入行](writing.zh-CN.md) 与 [读取行](reading.zh-CN.md) —— 会检查列名与类型的表操作语句。
- [示例](cookbook.zh-CN.md) —— 常见用法。
