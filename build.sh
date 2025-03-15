#!/bin/bash
echo "Checking Java installation"
java -version
echo "Setting up JAVA_HOME"
export JAVA_HOME=$(dirname $(dirname $(which java)))
echo "JAVA_HOME set to: $JAVA_HOME"
echo "Making Maven wrapper executable"
chmod +x ./mvnw
echo "Running Maven build"
./mvnw clean package -DskipTests