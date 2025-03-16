FROM openjdk:10

WORKDIR /app

COPY . .

# Make build script executable
RUN chmod +x ./build.sh

# Run build script with detailed output
RUN bash -x ./build.sh || { echo "Build script failed"; exit 1; }

CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]