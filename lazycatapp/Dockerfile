FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制构建产物
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 