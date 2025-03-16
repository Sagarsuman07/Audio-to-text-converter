#!/bin/bash
set -e
set -x

echo "Checking Java installation"
java -version || { echo "Java not found in Docker container, which is unexpected"; exit 1; }

echo "Setting up JAVA_HOME"
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo "JAVA_HOME set to: $JAVA_HOME"

echo "Making Maven wrapper executable"
chmod +x ./mvnw

echo "Running Maven build"
./mvnw clean package -DskipTests