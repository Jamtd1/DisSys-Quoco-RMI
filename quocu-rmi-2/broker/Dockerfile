FROM openjdk:8-jre-alpine
COPY target/broker-*-jar-with-dependencies.jar /broker.jar
CMD ["/usr/bin/java", "-jar", "/broker.jar"]
