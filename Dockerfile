# Stage 1: Build your app
FROM gradle:8.5.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Stage 2: Run your app with minimal runtime
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl

WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
