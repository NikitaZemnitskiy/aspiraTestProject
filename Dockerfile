# Этап 1: Сборка приложения и копирование зависимостей
FROM maven:3.9.9-ibm-semeru-21-jammy AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN mvn dependency:copy-dependencies

FROM openjdk:21

WORKDIR /app

COPY --from=build /app/target/aspiraTestProject-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/dependency/ /app/libs/

ENTRYPOINT ["java", "-cp", "app.jar:libs/*", "com.zemnitskiy.Main"]