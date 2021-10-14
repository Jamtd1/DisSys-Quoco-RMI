FROM openjdk:8-jre-alpine
ENV HOST=localhost
COPY target/*-jar-with-dependencies.jar /auldfellas.jar
# CMD ["/usr/bin/java", "-jar", "/auldfellas.jar"]
CMD java -jar /auldfellas.jar $HOST
