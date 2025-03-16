FROM maven:3.8-openjdk-17

WORKDIR /app

COPY . .

# Run Maven build
RUN mvn clean package -DskipTests

# Use the built JAR
CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]