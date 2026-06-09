# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Create dummy module directories
RUN mkdir -p desktop android web app shared/core shared/player server models

# Copy build files (use simple COPY without shell redirection)
COPY models/build.gradle.kts models
COPY shared/core/build.gradle.kts shared/core
COPY shared/player/build.gradle.kts shared/player
COPY web/build.gradle.kts web

# Copy source code - use simple COPY
COPY models/src models/src
COPY shared/core/src shared/core/src
COPY shared/player/src shared/player/src
COPY web/src web/src

COPY properties/web.properties ./local.properties

RUN chmod +x gradlew

# Build only the web
RUN ./gradlew :web:wasmJsBrowserDistribution --no-daemon

# Stage 2: Runtime stage
FROM nginx:alpine AS runtime

COPY --from=build /app/web/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

RUN chown -R dndhelper:dndhelper /app

EXPOSE 8081

CMD ["nginx", "-g", "daemon off;"]
