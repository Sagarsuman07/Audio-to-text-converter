FROM openjdk:10
WORKDIR /app
COPY . .
RUN chmod +x ./build.sh
RUN ./build.sh
CMD ["java", "-jar", "target/audiototext-0.0.1-SNAPSHOT.jar"]