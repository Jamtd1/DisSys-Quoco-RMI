FROM openjdk:8-jre-alpine
ENV HOST=localhost
COPY target/*-jar-with-dependencies.jar /client.jar
# CMD ["/usr/bin/java", "-jar", "/auldfellas.jar"]
CMD java -jar /client.jar $HOST
