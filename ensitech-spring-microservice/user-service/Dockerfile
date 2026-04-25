FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install netcat to check if dependent services are available
RUN apt-get update && apt-get install -y netcat-openbsd

# Copy the generated JAR file into the container
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8081

# Wait for discovery-service and config-service before starting
CMD sh -c "while ! nc -z discovery-service 8761; do echo 'Waiting for Discovery...'; sleep 3; done && \
           while ! nc -z config-service 9999; do echo 'Waiting for Config...'; sleep 3; done && \
           echo 'Starting User Service...' && java -jar app.jar"