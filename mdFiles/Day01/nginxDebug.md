# 苍穹外卖项目学习排障记录

记录日期：2026-05-26  
项目目录：`/Users/mellowleo/SkyEats`

## 背景

当前学习的是黑马程序员《苍穹外卖》项目。这个教程原本主要是后端 15 天内容，后来又补充了前端 3 天内容。

学习环境是 Mac，不是 Windows。视频中的前端运行环境使用了 Windows 版 `nginx.exe`，因此不能直接在 Mac 上运行。

## 资料和 nginx 情况

一开始在已经解压出来的资料目录里没有找到视频中展示的 `nginx-1.20.2` 目录。

后来确认到：`nginx-1.20.2` 实际藏在顶层压缩包：

```text
/Users/mellowleo/SkyEats/1、黑马程序员Java项目《苍穹外卖》企业级开发实战/资料.rar
```

压缩包中的路径是：

```text
资料/day01/前端运行环境/nginx-1.20.2
```

曾经把其中的 `前端运行环境` 解压到了：

```text
/Users/mellowleo/SkyEats/1、黑马程序员Java项目《苍穹外卖》企业级开发实战/苍穹外卖前端课程/资料/day01/前端运行环境
```

其中包含：

```text
nginx-1.20.2/nginx.exe
nginx-1.20.2/conf/nginx.conf
nginx-1.20.2/html/sky
nginx-1.20.2/logs
nginx-1.20.2/temp
```

但是因为 `nginx.exe` 是 Windows 程序，Mac 不能运行它。

## 关于 Mac 是否必须使用 nginx

结论：当前学习后端主线时，Mac 上暂时不需要使用 nginx。

视频里的 nginx 主要作用是：

```text
浏览器 -> nginx -> Java 后端 8080
```

nginx 做两件事：

```text
1. 托管前端静态页面
2. 把 /api 请求反向代理到后端
```

Mac 开发阶段可以用 Vue 自带的开发服务器替代 nginx：

```text
浏览器 -> Vue devServer 8888 -> Java 后端 8080
```

因此当前推荐路线是：

```text
前端：npm run serve，访问 http://localhost:8888
后端：IDEA 启动 SkyApplication，监听 http://localhost:8080
```

等后面学到部署、反向代理或上线时，再考虑安装 Mac 版 nginx。

## 前端项目位置

前端源码目录：

```text
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts
```

前端配置中已经有开发代理。

`.env.development` 中：

```text
VUE_APP_URL = 'http://localhost:8080/admin'
```

`vue.config.js` 中：

```js
devServer: {
  port: 8888,
  proxy: {
    '/api': {
      target: process.env.VUE_APP_URL,
      pathRewrite: {
        '^/api': ''
      }
    }
  }
}
```

也就是说，开发时前端请求 `/api` 会被转发到：

```text
http://localhost:8080/admin
```

## 已经遇到并处理过的问题

### 1. npm install 依赖冲突

一开始执行：

```bash
npm install
```

报错：

```text
ERESOLVE could not resolve
```

原因是该项目是老 Vue2 项目，依赖版本比较旧，npm 7/8 对 peer dependency 检查更严格。

处理方式：

```bash
npm install --legacy-peer-deps
```

### 2. 旧淘宝 npm 源证书过期

继续安装时报错：

```text
CERT_HAS_EXPIRED
request to https://registry.npm.taobao.org/...
```

原因是项目自带的 `package-lock.json` 里锁了大量旧地址：

```text
https://registry.npm.taobao.org/...
```

这个旧源证书过期，即使设置了新的 registry，npm 仍可能优先使用 lock 文件中的旧地址。

处理方式：

```bash
npm install --legacy-peer-deps --no-package-lock --registry=https://registry.npmmirror.com
```

### 3. fibers 导致 npm run serve 崩溃

安装依赖成功后，执行：

```bash
npm run serve
```

报错：

```text
Assertion failed: (thread_id_key != 0x7777), function find_thread_id_key, file coroutine.cc, line 134.
zsh: abort npm run serve
```

原因是老依赖 `fibers` 和当前 Mac/Node 环境不兼容。`fibers` 主要用于 Sass 编译加速，不是业务必需依赖。

已经修改：

文件：

```text
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/package.json
```

删除：

```json
"fibers": "^4.0.2"
```

并把：

```json
"sass": "^1.22.10"
```

改成：

```json
"sass": "1.32.13"
```

同时删除了本地：

```text
node_modules/fibers
```

### 4. vue-router 类型导入报错

前端服务启动后，出现 TypeScript error：

```text
Module 'vue-router/types' has no exported member 'Route'
```

原因是安装到的 `vue-router` 版本为 `3.6.5`，`Route` 类型不再从 `vue-router` 顶层导出，而是在：

```text
vue-router/types/router
```

已经修改了以下文件：

```text
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/src/views/login/index.vue
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/src/permission.ts
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/src/layout/components/Sidebar/SidebarItem.vue
/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/src/components/Breadcrumb/index.vue
```

把：

```ts
import { Route } from 'vue-router'
```

或类似导入改成：

```ts
import { Route } from 'vue-router/types/router'
```

如果同时导入了 `RouteConfig`、`RouteRecord`，也改为从：

```ts
vue-router/types/router
```

导入。

## 当前前端状态

前端已经能启动并显示登录页。

访问地址：

```text
http://localhost:8888
```

页面显示了苍穹外卖登录页，默认账号密码类似：

```text
账号：admin
密码：123456
```

这说明前端环境基本跑通。

## 为什么点击登录没有反应

已经检查过：

```text
8888 有服务在监听，说明前端在运行
8080 没有服务在监听，说明后端还没有启动
```

所以点击登录没有反应是正常的，因为前端会请求后端：

```text
http://localhost:8080/admin/employee/login
```

但当前后端服务还没有启动。

## 下一步应该做什么

继续看后端教程，而不是继续纠结 nginx。

推荐继续学习：

```text
Day01-05 后端环境搭建
Day01-06 后端工程导入
Day01-07 数据库环境
Day01-08 配置文件/启动后端
```

目标是用 IDEA 启动后端主类：

```text
com.sky.SkyApplication
```

后端成功启动后应监听：

```text
http://localhost:8080
```

完整学习开发状态应该是：

```text
前端：http://localhost:8888
后端：http://localhost:8080
```

然后再回到前端登录页测试登录。

如果登录失败，下一步要检查：

```text
1. 后端是否启动成功
2. MySQL 是否启动
3. 数据库 sky_take_out 是否导入
4. application-dev.yml 数据库账号密码是否正确
5. 浏览器控制台 Network 中登录请求的响应
6. IDEA 后端控制台报错信息
```

## 给后续 AI 的提醒

不要把当前问题继续判断成 nginx 问题。

当前真正的路线是：

```text
Mac 上开发阶段不用 Windows nginx.exe
前端已经通过 Vue devServer 跑在 8888
下一步是启动 Java 后端 8080 和配置数据库
```

如果后续前端依赖需要重装，建议使用：

```bash
cd "/Users/mellowleo/SkyEats/Front-endCode/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts"
npm install --legacy-peer-deps --no-package-lock --registry=https://registry.npmmirror.com
```

如果再次出现 `fibers` 相关 `coroutine.cc` 崩溃，需要确认：

```text
package.json 中没有 fibers
node_modules/fibers 已删除
sass 固定为 1.32.13 或其他兼容旧 Vue CLI 的版本
```

