# 原始语句

**简体中文** | [English](raw-statements.md)

**原始语句是 unsafe 操作，没有任何保底机制。** 插件会把语句原样发给服务端：不解析、不与已注册的表比对、不检查其中提到的库、表、列是否存在，也不为不同实现做转换。连接所用账号能做的事，原始语句都能做——包括删表、删库、让服务端停机。它适合迁移，以及声明式语法表达不了的服务端特有语句。**不要**用它绕过插件已经拒绝的声明；如果某个拒绝本身是错的，那是一个该修的 bug，而不是该写 SQL 的理由。

本文档其他页面里的语句，都建立在你注册过的表之上。只有这一页不是。

## 什么时候该用它

- **迁移。** `ALTER TABLE`、`CREATE INDEX`、数据修补——声明式语法没有对应写法的事。
- **只有你的服务端懂的语句。** 厂商扩展、优化器提示、可移植写法覆盖不到的东西。
- **声明表达不了的形状。** 连接查询、计算列、`GROUP BY`。
- **动态过滤。** 运行时才拼出来的 `WHERE` 或 `ORDER BY`：`where` 块表达不了它，因为列名不是值。

声明式语句能做的事，都该由声明式语句做：它在发出前被检查、会把值转换成列自己的类型、也不需要你懂 SQL。原始语句是留给剩下的那部分的。

## SQL 语句

两条语句，因为关系型服务端的回答有两种形状：行，或受影响的行数。

```sk
execute query "SELECT id, name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}
execute update "ALTER TABLE users ADD COLUMN age INT NULL"
execute update "UPDATE users SET age = ? WHERE name = ?" with (30, "Alice") and store affected rows in {_rows}
```

- **`execute query`** 存结果行。键是服务端报告的列标签，行号从 1 起，与 `select many` 的形状完全一致：`{_rows::1::name}`、`{_rows::2::name}`。别名就是你读的名字，所以 `SELECT name AS who` 给的是 `{_rows::1::who}`。没有匹配到行就是什么都不存，这不算失败。
- **`execute update`** 存受影响行数；省略该子句就什么都不存。服务端对不计数的语句（例如 DDL）报 `0`，这个 `0` 就照原样存下来。
- **两者都会等待。** 后面的行在数据库回答之后才执行；`and wait` 可以写，但不改变任何行为。

## MongoDB 命令

文档数据库收的是文档，每条命令也回一个文档。这和上面两种形状都不同，所以它是一条**独立的语句**，而不是某条语句的另一种写法：

```sk
execute command "{ ""count"": ""users"" }" and store the result in {_answer::*}
send "现在有 %{_answer::n}% 个用户。"
```

命令写成 JSON 文本，由驱动自己的解析器解析，所以 MongoDB 接受什么，这里就接受什么——包括它表示日期与对象 id 的扩展 JSON 形式。结果按原样存下来：字段按名字读，列表按下标读，例如 `{_answer::n}`、`{_answer::cursor::firstBatch::1::name}`。

**没有参数。** 命令本身就是整条语句，没有占位符可以绑值。

## 参数

在要放值的地方写 `?`，然后在 `with` 子句里按同样顺序给出值：

```sk
execute query "SELECT name FROM users WHERE age > ? AND language = ?" with (18, "zh-CN") and store the result in {_rows::*}
```

- 值的个数必须与语句里 `?` 的个数一致，不一致会在**发出之前**被拒绝。
- 值由驱动绑定，因此值永远不可能被当成 SQL 读取。这是本功能唯一提供的保护，也是**绝不该把值拼进语句**的原因。
- `?` 是按字面统计的，包括出现在字符串字面量或注释里的那些。如果你的语句里有这种 `?`，统计结果会和驱动的预期不符，驱动会用它自己的话说明。
- **参数必须是驱动能原样发送的值**：字符串、数字、布尔、字节数组、日期，或 `null`。UUID、物品、坐标等领域对象会被**拒绝而不是转换**——因为原始语句不指名任何列，也就没有可转换的目标类型。请在脚本里自己转换：UUID 转成字符串或 16 字节，物品转成其序列化字节。

## 哪些保证还成立

| 你习惯的保证 | 原始语句下 |
| --- | --- |
| 未知列名在语句发出前失败 | **不成立。** 由服务端在收到时判断。 |
| 注册时核对声明与磁盘上的表 | **不成立。** 原始语句不参与注册。 |
| 值会按列的类型与范围检查 | **不成立。** 值按原样绑定。 |
| 实现不读的连接属性被拒绝 | **成立。** 那条检查与语句无关。 |
| 缺连接时在运行前报错 | **成立。** 语句被拒绝，不会被发出。 |
| 失败进入 `last database error`，且是服务端原文 | **成立**，包含服务端的错误码。 |
| `?` 占位符不会被当作 SQL 读取 | **成立**，也是这里唯一的保护。 |
| 语句在 `database transaction` 内运行并随事务回滚 | **成立。** 原始语句属于它所在的事务。 |

## 连接不接受的那种语句

一条连接接受哪种原始语句，由**实现**决定，而不是由插件分情况判断：SQL 连接接受 `execute query` 与 `execute update`，文档连接接受 `execute command`。用错方向时会被拒绝，并且**报错里会点名该用哪种**，在语句到达服务端之前：

```sk
in connection "logs":          # 这是一条 MongoDB 连接
    execute query "SELECT 1" and store the result in {_rows::*}
```

```
Raw SQL statements are not supported by database 'MongoDB'. It takes raw commands instead:
'execute command "…"'. See the Raw statements page for what a raw statement does and does not guarantee.
```

## 账号权限就是上限

原始语句以连接的账号运行，只有该账号的权限。一个只能读写某个库里的行的账号，无法通过原始语句把库删掉；一个什么都能做的账号，在这里也什么都能做。

这是本功能留给你的唯一一根杠杆：**给连接它脚本需要的权限，而不是服务端管理员的权限。** [连接](connections.zh-CN.md#凭据)页对凭据说过同样的话。

## 参见

- [连接](connections.zh-CN.md) —— 原始语句以哪个账号运行。
- [表](tables.zh-CN.md) —— 声明式语句会核对什么，以及原始语句为什么做不到。
- [写入行](writing.zh-CN.md) 与 [读取行](reading.zh-CN.md) —— 会被检查的声明式写法。
- [菜谱](cookbook.zh-CN.md) —— 可直接抄的例子。
