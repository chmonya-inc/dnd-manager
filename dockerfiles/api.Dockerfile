# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy build files
COPY shared shared
COPY models models
COPY server server
COPY desktop desktop
COPY web web
COPY app app

RUN chmod +x gradlew

# Build only the server
RUN ./gradlew :server:installDist --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dndhelper && adduser -S dndhelper -G dndhelper

WORKDIR /app

COPY --from=build /app/server/build/install/server /app

RUN chown -R dndhelper:dndhelper /app

USER dndhelper

EXPOSE 9090

CMD ["./bin/server"]
