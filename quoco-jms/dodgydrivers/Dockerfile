FROM openjdk:8-jre-alpine
ENV HOST=localhost
COPY target/*-jar-with-dependencies.jar /dodgydrivers.jar
# CMD ["/usr/bin/java", "-jar", "/auldfellas.jar"]
CMD java -jar /dodgydrivers.jar $HOST
