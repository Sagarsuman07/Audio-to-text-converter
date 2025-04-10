FROM maven:3.8-openjdk-17

WORKDIR /app

# Install FFmpeg
RUN apt-get update && apt-get install -y ffmpeg && apt-get clean

# Copy project files
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Run the app
CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]
