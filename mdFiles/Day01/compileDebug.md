# Sky Take Out 项目编译问题排查记录（2026-05-29）

## 问题描述

初次导入 `sky-take-out` 项目后执行 Maven 编译：

```bash
mvn compile
```

出现编译失败：

```text
BUILD FAILURE
找不到符号
Could not find artifact
```

主要报错集中在：

```text
sky-common
WeChatPayUtil.java
```

错误类似：

```text
找不到符号
```

---

# 排查过程

---

## 第一步：确认 Maven 编译入口是否正确

一开始在：

```text
sky-server → Lifecycle → compile
```

执行编译。

报错：

```text
Could not find artifact com.sky:sky-common
Could not find artifact com.sky:sky-pojo
```

### 原因：

因为：

```text
sky-server
```

依赖：

```text
sky-common
sky-pojo
```

单独编译 `sky-server` 时，依赖模块尚未安装。

---

### 解决：

改为编译父工程：

```text
sky-take-out → Lifecycle → compile
```

---

# 第二步：排查 pom.xml 依赖配置

检查：

```text
sky-common/pom.xml
```

确认存在：

```xml
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-apache-httpclient</artifactId>
</dependency>
```

检查：

```text
sky-take-out/pom.xml
```

确认：

```xml
<dependencyManagement>
```

中已声明版本：

```xml
<version>0.4.8</version>
```

结论：

```text
pom.xml 配置正常
```

不是依赖缺失问题。

---

# 第三步：检查 Lombok

怀疑：

```text
@Data
@Getter
@Setter
```

没有生效。

进行了以下检查：

## 1）确认 Lombok 插件已安装

IDEA：

```text
Settings → Plugins → Lombok
```

检查结果：

```text
已安装
```

---

## 2）开启 Annotation Processing

路径：

```text
Settings
→ Build, Execution, Deployment
→ Compiler
→ Annotation Processors
```

开启：

```text
Enable annotation processing
```

重启 IDEA 后重新编译。

结果：

```text
问题仍然存在
```

---

# 第四步：检查 JDK 版本（最终定位）

查看项目 SDK：

```text
Project Structure → Project
```

发现：

```text
SDK = openjdk-26
```

即：

```text
JDK 26
```

项目使用：

```xml
Spring Boot 2.7.3
```

属于较老项目。

JDK 26 与部分旧依赖存在兼容性问题。

---

# 最终解决方案

下载：

```text
JDK 17
```

路径：

```text
Project Structure
→ Project
→ SDK
```

修改为：

```text
Microsoft OpenJDK 17
```

同时设置：

```text
Language Level = 17
```

然后：

Maven Reload：

```text
Maven → Reload Project
```

执行：

```bash
mvn clean
mvn compile
```

---

# 最终结果

编译成功：

```text
BUILD SUCCESS
```

日志显示：

```text
sky-take-out SUCCESS
sky-common   SUCCESS
sky-pojo     SUCCESS
sky-server   SUCCESS
```

---

# 最终结论

本次问题根因：

```text
本地 JDK 版本过高（JDK 26）
```

导致：

```text
Spring Boot 2.7.3 + Maven 多模块项目兼容异常
```

切换到：

```text
JDK 17
```

后恢复正常。

---

# 经验总结

以后遇到：

```text
BUILD FAILURE
找不到符号
依赖正常但编译失败
老师项目能跑，自己本地跑不了
```

优先排查顺序：

## ① JDK版本

优先确认：

```text
JDK 8 / 11 / 17
```

是否与项目匹配。

---

## ② Maven 是否 Reload

```text
Reload All Maven Projects
```

---

## ③ Lombok 插件

检查：

```text
插件是否安装
Annotation Processing 是否开启
```

---

## ④ 最后再考虑源码问题

如果老师源码可运行：

```text
优先怀疑环境
不要急着改代码
```

避免误改项目源码。
