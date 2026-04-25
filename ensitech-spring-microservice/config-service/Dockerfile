FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y netcat-openbsd

COPY target/*.jar app.jar

EXPOSE 9999

CMD sh -c "while ! nc -z discovery-service 8761; do echo 'Waiting for Discovery...'; sleep 3; done && \
           echo 'Starting Config Service...' && java -jar app.jar"