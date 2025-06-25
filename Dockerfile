# 使用多阶段构建
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行时镜像
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 创建非root用户
RUN groupadd -r fraud && useradd -r -g fraud fraud

# 复制构建的jar文件
COPY --from=builder /app/target/fraud-detection-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R fraud:fraud /app

# 切换到非root用户
USER fraud

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/fraud-detection/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 