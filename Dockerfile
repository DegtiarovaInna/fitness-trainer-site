# ---------- СТАДИЯ 1: билд Maven (JDK 21) ----------
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw mvnw
RUN ./mvnw -q package -DskipTests

# ---------- СТАДИЯ 2: рантайм (JRE 21) -------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# копируем собранный jar
COPY --from=builder /app/target/*.jar app.jar

# порт приложения
ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java","-jar","/app/app.jar"]