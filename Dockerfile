# Build the application
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .
RUN chmod +x gradlew
RUN ./gradlew test bootJar --no-daemon

# Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]