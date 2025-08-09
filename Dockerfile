
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw mvnw
RUN ./mvnw -q package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app


COPY --from=builder /app/target/*.jar app.jar


ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java","-jar","/app/app.jar"]