# 云序书集图书电子商务系统

基于 Spring、Spring MVC、MyBatis 的图书商城。页面使用 Thymeleaf 服务端渲染，本地开发使用 H2，云端部署使用 MySQL。前台提供商品浏览、购物车、结算和订单跟踪，后台提供商品、订单和用户管理。

在线访问：https://dagong-library-cloud-77rx9j6754ggcq4w-8080.app.github.dev/catalog

课程报告：[网站建设大作业-学号-姓名.docx](./网站建设大作业-学号-姓名.docx)

## 功能

- 公开商城：搜索、分类、价格和库存筛选，支持按销量或价格排序。
- 商品详情、购物车、校园配送或到店自提、下单、取消订单和确认收货。
- 后台经营数据、商品上下架、订单状态处理、用户角色调整。
- 首位注册账号自动成为管理员，后续注册账号默认为顾客。
- 密码使用 BCrypt 哈希保存。

## 主要地址

- `/catalog`：商城首页与分类检索
- `/products/{id}`：商品详情
- `/cart`：购物车
- `/checkout`：确认订单
- `/orders`：我的订单
- `/admin`：后台管理

## 本地运行

```bash
./mvnw test
./mvnw spring-boot:run
```

访问 `http://localhost:8080/catalog`。

## 云端运行

准备 MySQL 数据库并设置环境变量：

```bash
export SPRING_PROFILES_ACTIVE=cloud
export DB_URL='jdbc:mysql://127.0.0.1:3306/yunxu_books?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai'
export DB_USERNAME='store'
export DB_PASSWORD='your-password'
export DB_INIT_MODE=always
java -jar target/yunxu-books-1.0.0.jar
```

首次启动设置 `DB_INIT_MODE=always` 建表并导入商品数据，之后可改为 `never`。项目不预置登录账号，首次打开网站后从注册页创建管理员账号即可开始验收。
