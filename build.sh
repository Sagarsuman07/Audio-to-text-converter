#!/bin/bash
echo "Checking Java installation"
java -version || { echo "Java not found, please install Java"; exit 1; }

echo "Setting up JAVA_HOME"
# More reliable way to find JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
  java_path=$(readlink -f $(which java))
  # Remove the /bin/java part
  export JAVA_HOME=${java_path%/bin/java}
  # Alternative approach if the above doesn't work
  # export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
fi

echo "JAVA_HOME set to: $JAVA_HOME"
echo "Making Maven wrapper executable"
chmod +x ./mvnw
echo "Running Maven build"
./mvnw clean package -DskipTests