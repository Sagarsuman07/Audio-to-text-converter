# Stage 1: Build with Maven
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Stage 2: Runtime with FFmpeg support
FROM openjdk:17-slim

# Install FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy only the final JAR from the builder stage
COPY --from=builder /app/target/audiototext-0.0.1-SNAPSHOT.jar .

# Run the app
CMD ["java", "-jar", "audiototext-0.0.1-SNAPSHOT.jar"]
