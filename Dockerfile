# Stage 1: Build the application using Gradle and JDK
FROM docker.io/library/gradle:9.1.0-jdk25-alpine AS build

# Set the working directory in the build stage
WORKDIR /app

# Copy the source code into the container
COPY . .

# Build the application without running the tests
RUN gradle build --no-daemon -x test

# Stage 2: Create the runtime container
FROM docker.io/library/eclipse-temurin:25-jre-alpine

# Set the working directory in the runtime container
WORKDIR /app

# Add a non-root user boot:spring
RUN addgroup -S spring && adduser -S boot -G spring

# Set the timezone environment variable to UTC+7
ENV TZ=Asia/Bangkok

# Copy the built JAR file from the build stage to the runtime container
# Set ownership of the JAR file to boot:spring during the copy
COPY --from=build --chown=boot:spring /app/build/libs/*.jar app.jar

# Set application listen port
ENV APP_LISTEN_PORT=8080

# Expose port 8080 for the application
EXPOSE 8080

# Run the container as user 'boot'
USER boot:spring

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
