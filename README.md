# 大工云图书馆

基于 Spring、Spring MVC、MyBatis 的图书管理系统。页面使用 Thymeleaf 服务端渲染，本地开发使用 H2，云端部署使用 PostgreSQL。

在线访问：https://dagong-cloud-library.onrender.com/catalog

## 功能

- 公开馆藏检索：书名、作者、ISBN、索书号、分类和可借状态。
- 读者注册、登录、预约借阅、取消预约和办理归还。
- 馆员入藏、编辑、删除图书，确认取书和办理归还。
- 首个注册账号自动获得馆员权限，后续注册账号为读者。
- 密码使用 BCrypt 哈希保存。

## 本地运行

```bash
./mvnw test
./mvnw spring-boot:run
```

访问 `http://localhost:8080/catalog`。

## 云端运行

准备 PostgreSQL 数据库并设置环境变量：

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DATABASE_URL='postgresql://user:password@host/database?sslmode=require'
java -jar target/dagong-cloud-library-1.0.0.jar
```

首次启动会自动建表并导入馆藏数据，后续启动不会覆盖已有用户和借阅记录。

## 测试账号

项目不预置登录账号。首次打开网站后，从注册页创建馆员账号，再注册读者账号即可完整验收。
