FROM openjdk:20-slim
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} app.jar
VOLUME /syntechnica
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]