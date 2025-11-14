# Step 1: Use a base image with Java 17 (or your Java version)
FROM eclipse-temurin:17-jdk-alpine

# Step 2: Create a directory for the app
WORKDIR /app

# Step 3: Copy the built JAR file from target/
COPY target/*.jar app.jar

# Step 4: Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Step 5: Pass environment variables at runtime (not here — for security)
# They’ll be injected when running the container

# Step 6: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
