# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install bash and gradle (and git/ca-certificates which are often needed)
RUN apk add --no-cache bash gradle git ca-certificates \
 && update-ca-certificates || true

# Copy root build files
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the eureka-server directory
COPY eureka-server/ eureka-server/

# Build the specific module using installed gradle
RUN gradle :eureka-server:clean :eureka-server:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-server/build/libs/eureka-server-*.jar app.jar
EXPOSE 8761
RUN apk add --no-cache curl
ENTRYPOINT ["java","-jar","/app/app.jar"]