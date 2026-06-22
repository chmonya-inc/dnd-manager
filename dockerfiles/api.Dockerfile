# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Gradle dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle gradle
COPY gradlew .
RUN chmod +x gradlew

RUN mkdir -p shared/core shared/desktop shared/player models server desktop web app

COPY shared/core/build.gradle.kts shared/core/
COPY shared/desktop/build.gradle.kts shared/desktop/
COPY shared/player/build.gradle.kts shared/player/
COPY models/build.gradle.kts models/
COPY server/build.gradle.kts server/
COPY desktop/build.gradle.kts desktop/
COPY web/build.gradle.kts web/
COPY app/build.gradle.kts app/

# Load dependencies
RUN ./gradlew dependencies --no-daemon

# Copy build files
COPY shared/core shared/core
COPY models models
COPY server server

# Build
RUN ./gradlew :server:installDist --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dndhelper && adduser -S dndhelper -G dndhelper

WORKDIR /app

COPY --from=build /app/server/build/install/server /app

RUN chown -R dndhelper:dndhelper /app

USER dndhelper

EXPOSE 9090

CMD ["./bin/server"]