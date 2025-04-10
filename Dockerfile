FROM maven:3.8-openjdk-17

# Switch to root to install ffmpeg
USER root

# Install ffmpeg and dependencies
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy all project files
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Expose port (optional, in case you use it)
# EXPOSE 8080

# Run the app
CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]
