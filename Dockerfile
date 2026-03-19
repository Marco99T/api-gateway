# Etapa 1: build
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .

COPY src/main/resources/keys /app/keys

RUN mvn clean package -DskipTests

# Etapa 2: runtime
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


# LOCAL
# FROM eclipse-temurin:17-jre-alpine

# WORKDIR /app

# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar

# EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "/app/app.jar"]