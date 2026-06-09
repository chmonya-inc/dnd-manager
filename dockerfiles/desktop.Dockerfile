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

COPY properties/desktop.properties ./local.properties

RUN chmod +x gradlew

# Build only the web
RUN ./gradlew :desktop:run --no-daemon
