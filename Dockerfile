FROM maven:3.8-openjdk-10

WORKDIR /app

COPY . .

# Run Maven directly instead of using the wrapper
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]