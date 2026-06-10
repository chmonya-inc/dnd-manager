# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

COPY . .

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
