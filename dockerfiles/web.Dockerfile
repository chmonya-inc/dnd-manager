# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Create dummy module directories
RUN mkdir -p desktop android web app shared server models

# Copy build files (use simple COPY without shell redirection)
COPY shared/build.gradle.kts shared
COPY models/build.gradle.kts models
COPY web/build.gradle.kts web

# Copy source code - use simple COPY
COPY models/src models/src
COPY shared/core/src shared/core/src
COPY shared/player/src shared/player/src
COPY web/src web/src

COPY apps-script apps-script

COPY properties/web.properties ./local.properties

RUN chmod +x gradlew

# Build only the web
RUN ./gradlew :web:wasmJsBrowserDistribution --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dndhelper && adduser -S dndhelper -G dndhelper

WORKDIR /app

COPY --from=build /app/web/build/install/web /app
COPY --from=build /app/apps-script ./apps-script

RUN chown -R dndhelper:dndhelper /app

USER dndhelper

EXPOSE 8081

CMD ["./bin/web"]
