# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
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

ARG APPS_SCRIPT_URL_ANDROID
ARG APPS_SCRIPT_URL_DESKTOP
ARG IMGBB_API_KEY

ENV APPS_SCRIPT_URL_ANDROID=$APPS_SCRIPT_URL_ANDROID
ENV APPS_SCRIPT_URL_DESKTOP=$APPS_SCRIPT_URL_DESKTOP
ENV IMGBB_API_KEY=$IMGBB_API_KEY
ENV NODE_OPTIONS=--max-old-space-size=2048

# Build only the web
RUN ./gradlew :web:wasmJsBrowserDistribution --no-daemon --max-workers=1

# Stage 2: Runtime stage
FROM nginx:alpine AS runtime

COPY --from=build /app/web/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
