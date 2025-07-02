# Use an official Gradle image to build the app
FROM gradle:8.5.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test

# Use a lightweight JDK image to run the app
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /home/gradle/src/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]