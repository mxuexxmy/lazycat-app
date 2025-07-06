# lazycat-app
懒猫应用

懒猫微服应用扩展

基于懒猫微服的接口进行开发的应用

功能特色
1. 排行榜
2. 统计分析
3. 移植开发者信息统计
4. 评论查看
5. 应用搜索支持应用名称、包名、关键词、源码地址、应用描述、简介、分类

# 运行
## 后端
### 运行
```shell
./mvnw spring-boot:run
```
### 打包
```shell
mvn clean package -DskipTests
```

## 前端
### 运行
```shell
npm run dev
```
## 打包
```shell
npm run build
```