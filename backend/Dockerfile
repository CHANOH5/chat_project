FROM openjdk:17-jdk

EXPOSE 8080

VOLUME /data

COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]