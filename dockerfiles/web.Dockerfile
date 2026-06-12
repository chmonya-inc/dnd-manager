# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY properties/web.properties ./local.properties

# Copy build files
COPY shared .
COPY models .
COPY web .

RUN chmod +x gradlew

# Build only the web
RUN ./gradlew :web:wasmJsBrowserDevelopmentExecutableDistribution --no-daemon

# Stage 2: Runtime stage
FROM nginx:alpine AS runtime

COPY --from=build /app/web/build/dist/wasmJs/developmentExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
