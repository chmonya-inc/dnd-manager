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
COPY desktop/build.gradle.kts desktop

# Copy source code - use simple COPY
COPY shared/src shared/src
COPY models/src models/src
COPY desktop/src desktop/src

COPY apps-script apps-script

RUN chmod +x gradlew

# Build only the web
RUN ./gradlew :desktop:run --no-daemon

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
