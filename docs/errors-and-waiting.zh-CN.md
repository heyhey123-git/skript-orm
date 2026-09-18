# 错误与等待

**简体中文** | [English](errors-and-waiting.md)

每条碰数据库的语句都会等自己的活儿干完。它之后的语句稍后才执行，局部变量在此期间保持自己的值，而失败会等在 `last database error` 里。脚本的书写顺序因此就是语句的执行顺序，这里也不存在“悄悄稍后才发生”的写入。

## 哪些会等

| Section | 是否等待 | 说明 |
| --- | --- | --- |
| `create a connection` | 总是 | 不接受也不需要 `and wait`。 |
| `register a database table` | 总是 | 同上。 |
| `in connection` | 不等 | 块里的语句各自决定；切换本身不做数据库工作。 |
| `use connection`、`make ... the default` | 不等 | 它们只改变后续语句用哪条连接。 |
| `select one`、`select many`、`select page`、`select ... by id` | 总是 | 读操作没拿到行之前无事可做。 |
| `insert`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete` | 总是 | 写入之后的语句等改动被数据库接收后才执行。 |
| `disconnect ...` | 下一行会等 | 异步执行，但 trigger 会在它结束之后继续。各写法都不报成功。 |

会等的语句和 Skript 里其它延迟部分一样：它之后的语句稍后才执行。**等待期间并不占着服务器主线程**，别的玩家、别的脚本照常运行，被停住的只是这一条 trigger。

`and wait` 在每条读、每条写上仍然照收，只是不起作用。它曾经是写入用来要求“现在这个行为”的写法，所以这几页的示例都还留着它：给 1.1 写的脚本一行都不用改，而它要求的等待本来就已经有了。

## last database error

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
if last database error is set:
    send "写入失败: %last database error%" to console
    stop
send "已保存。" to console
```

- 它属于操作所在的**事件**。两个玩家执行同一条命令，各自有一份；在之后无关的事件里读它说明不了任何事。
- 每个操作**在开始前会清掉它**，所以你读到的是刚刚那次操作的结果，而不是更早的。**在事务里则特意不清**：出错的那条语句把原因留在那里，它后面的语句被跳过、自己什么也不报告，这时清掉它只会让脚本什么都读不到。见 [事务](transactions.zh-CN.md)。
- `store affected rows` 的变量正好相反：**每条**写了这个子句的语句都会清空它，事务里也一样，因为数字绝不该比产生它的那条语句活得更久。见 [影响行数](affected-rows.zh-CN.md)。
- 没有错误时它渲染成 `<none>`，所以请用 `is set` 判断，而不是拿文本比较。
- 失败会设置它，成功则让它保持为空：干得好好的语句没什么可说的，所以它后面的语句可以把“空”当作成功。
- **失败不会停住 trigger。** 失败的语句之后的语句照常执行，所以本页的例子都是自己检查槽位、自己 `stop`。后面几句依赖"这次确实写进去了"的脚本，也得这么做。
- 失败同时会连着脚本行号写进控制台。脚本读的是那个槽位，管理员读的是控制台那行。

## 先写后读

下一行执行时写入已经结束了，所以紧跟其后的读取看得到它：

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

“先读、再根据它写入”以及“先删、再插”也一样。这个顺序不需要额外写什么，它本来就是语句的行为。

## 不是数据库的失败

有些失败发生在发出语句之前，但报告方式相同：这个连接上从未注册过的表、表描述里没有的列、Skript 无法转换成
该列类型的值、以及同一张表的第二次注册。它们都会紧接着出现在 `last database error` 里。而数据库自己拒绝的事情，
比如在 `not null` 列上写 `null`、或者值比列还长，也一样会出现在那里：语句会等到数据库回话之后，才让它后面的语句执行。
