
# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install bash
RUN apk add --no-cache bash

# Copy Gradle wrapper and build files first for better caching
COPY gradlew ./
COPY gradle ./gradle
COPY gradle/wrapper ./gradle/wrapper
COPY build.gradle.kts ./
COPY settings.gradle.kts ./

# Pre-download dependencies (will be cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy the user-service source
COPY . ./

# Build the application
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/build/libs/user-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
