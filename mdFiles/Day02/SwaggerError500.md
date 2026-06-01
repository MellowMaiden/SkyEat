# 新增员工接口 Debug 记录

## 问题现象

在 Swagger 中测试新增员工接口：

```http
POST /admin/employee
```

返回结果：

```json
{
  "status": 500,
  "error": "Internal Server Error"
}
```

---

## 问题排查

开始以为是：

* JWT 校验失败
* 请求参数格式错误
* Swagger 调试问题

后面检查 `EmployeeMapper.java` 中新增员工 SQL，发现问题出在 `insert` 语句。

```java
@Insert("insert into employee (name,username,password,phone,sex,id_number,create_time,update_time,create_user,update_user)" +
        "values " +
        "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
```

---

## 原因分析

SQL 中：

* 字段数量：10 个
* values 中参数数量：11 个

多写了：

```java
#{status}
```

导致执行 SQL 时字段和值数量不匹配，数据库报错，因此 Swagger 返回：

```text
500 Internal Server Error
```

---

## Debug 总结

本次报错不是：

* Swagger 配置问题
* JWT 权限问题
* 前端请求参数问题

而是：

> `EmployeeMapper` 中 `insert` SQL 字段数量与 values 数量不一致导致数据库执行失败。

---

## 收获

排查接口 500 错误时，可以优先检查：

* Controller 是否接收到请求
* Service 业务逻辑是否正常
* Mapper SQL 是否正确
* 数据库字段数量、参数数量是否一致

以后遇到：

```text
500 Internal Server Error
```

优先看后端控制台报错日志，定位会更快。
