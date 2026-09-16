# 错误与等待

**简体中文** | [English](errors-and-waiting.md)

脚本能看到什么，取决于两件事：这次操作是否等待，以及 `last database error`。

## 哪些会等

| Section | 是否等待 | 说明 |
| --- | --- | --- |
| `create a connection` | 总是 | 不接受也不需要 `and wait`。 |
| `register a database table` | 总是 | 同上。 |
| `select one`、`select many`、`select page`、`select ... by id` | 总是 | 读操作没拿到行之前无事可做。 |
| `insert`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete` | 写了 `and wait` 才等 | 不写就交给后台。 |
| `disconnect from the current database` | 下一行会等 | 异步执行，但 trigger 会在它结束之后继续。 |

会等的 section 和 Skript 里其它延迟部分一样：它之后的语句稍后才执行，而局部变量在此期间保持自己的值。

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
- 每个操作**在开始前会清掉它**，所以你读到的是刚刚那次操作的结果，而不是更早的。
- 没有错误时它渲染成 `<none>`，所以请用 `is set` 判断，而不是拿文本比较。
- 失败会设置它、成功会留下空的它 —— 但下一节会说，“空的”这句话比听起来弱。

## 不等待的写入不会告诉你什么

不写 `and wait` 时，section 把工作交给后台，下一行立刻执行：

```sk
insert one entity into table "users":
    values:
        name: "Alice"
# 在写入完成之前就执行了；这次写入的失败也只会写进日志
if last database error is set:
    send "这里不会报告失败的写入。" to console
```

不等待时仍然会报告的，是 section 当场能做的检查：没有当前数据库、表不存在、值转换不了、`where` 条件里写了表里
没有的列。**不会**报告的是数据库自己拒绝的事情 —— 那些会连着脚本行号写进控制台。所以在不等待的情况下：

- 失败的写入可能让错误保持为空。
- 成功的写入同样让它保持为空。

脚本需要知道结果时，就写 `and wait`。

## 先写后读

读取一定会等，而不带 `and wait` 的写入不会，所以下面这段可能读到旧数据：

```sk
insert one entity into table "users" and wait:   # 这个 wait 才让后面的读取可靠
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

“先读、再根据它写入”以及“先删、再插”也一样。顺序有影响时，就写 `and wait`，让脚本把这件事说清楚。

## 不是数据库的失败

有些失败发生在发出语句之前，但报告方式相同：这个连接上从未注册过的表、表描述里没有的列、列装不下的值、在
`not null` 列上写 `null`、以及同名表的第二次注册。它们都会紧接着出现在 `last database error` 里。
