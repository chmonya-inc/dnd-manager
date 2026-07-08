ARG APPS_SCRIPT_URL_ANDROID
ARG APPS_SCRIPT_URL_DESKTOP
ARG IMGBB_API_KEY

# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

ENV APPS_SCRIPT_URL_ANDROID=$APPS_SCRIPT_URL_ANDROID \
    APPS_SCRIPT_URL_DESKTOP=$APPS_SCRIPT_URL_DESKTOP \
    IMGBB_API_KEY=$IMGBB_API_KEY \
    NODE_OPTIONS=--max-old-space-size=4096 \
    DOCKER=true

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
COPY shared/player shared/player
COPY models models
COPY server server
COPY web web

# Build only the web (using Production for small size and better optimization)
RUN ./gradlew :web:wasmJsBrowserDistribution --no-daemon --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4096m

# Stage 2: Runtime stage
FROM nginx:alpine AS runtime

COPY --from=build /app/web/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
