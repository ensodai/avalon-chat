# --- Stage 1: Сборка FatJar сервера ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache bash

# Копируем Gradle обертку и конфигурации
COPY gradlew ./
COPY gradle/ gradle/
COPY gradle.properties settings.gradle.kts build.gradle.kts ./

# Копируем необходимые для бэкенда модули (:shared и :server)
COPY shared/ shared/
COPY server/ server/

# Собираем исполняемый FatJar сервера
RUN chmod +x gradlew && ./gradlew :server:shadowJar --no-daemon -x test

# --- Stage 2: Финальный runtime-образ ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl tzdata

COPY --from=build /app/server/build/libs/flow-backend.jar app.jar

# Хранилище загружаемых медиафайлов
RUN mkdir -p /app/uploads
VOLUME /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]