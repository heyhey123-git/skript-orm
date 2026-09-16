# 更新与删除

**简体中文** | [English](updating-and-deleting.md)

两者各有两种形态：按 `where` 块（可以匹配多行）和按主键（只匹配一行）。

## 按条件更新

```sk
update entities in table "users" with limit 10 and wait:
    values:
        active: false
    where all:
        last_seen < {_cutoff}
        active = true
if last database error is set:
    send "更新失败: %last database error%" to console
```

主体里是 `values` 块和一个可选的 `where` 块，顺序随意；值的写法和插入完全一致：没写的列不会被碰，字面量 `null`
才存 SQL NULL。见 [写入行](writing.zh-CN.md)。

## 按 id 更新

```sk
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

它不接受 `where` 块：更新的是“已注册主键等于该值”的那一行。

## 按条件删除

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

删除没有 values 块，能写的只有可选的 `where`、可选的 limit，以及 `and wait`。

## 按 id 删除

```sk
delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "删除失败: %last database error%" to console
```

冒号标出的是有正文的语句。这条没有正文；从变量取值的 `update`、`upsert` 与 `insert` 也没有，它们都不写冒号，见 [写入行](writing.zh-CN.md)。

## 不写 where 会怎样

**没有 `where` 块的 `update entities` 和 `delete entities` 会作用于实现允许它作用的所有行。** 两个 section 都
接受这种写法，所以漏写 `where` 不会报错：

```sk
# 表里的每一行
delete entities from table "users" and wait
```

而 `where` 块里条件列表为空，几乎总是笔误。带 values 块的 section，例如 `update`，会直接拒绝它；单纯的删除却无从判断，只能照做。真要对所有行动手，就把它写明，并且别忘了 `with limit`。

## limit

`with limit N` 表示最多 N 行，且必须为正。在 MySQL 上它会变成 `... LIMIT N`，这是 MySQL 的特性；无法表达它的实现
会明确报错，而不是悄悄忽略。

limit 是保险，不是分页手段：留下哪几行由数据库决定，所以让更新/删除可预期的是 `where` 块。

## 等待

两个 section 都接受 `and wait`。带上它，后续语句在改动结束后执行、失败可在 `last database error` 里读到；不带它，
工作交给后台，数据库层面的失败只会写日志。同步检查（没有连接、表不存在、值放不下、limit 为零）无论哪种情况都会出现在
`last database error` 里。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 从变量更新

`update` 与 `upsert` 都接受“形状像查询结果的变量”作为新值，这也是“读出来、改一改、存回去”最短的写法：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
update one entity {_user::*} in table "users" by id {_user::id} and wait
```

用 `update` 时只有变量里出现的列会被碰；用 `upsert` 时主键还不存在的话会新建那一行。见 [菜谱](cookbook.zh-CN.md)。
