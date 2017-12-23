FROM openjdk:8-jre

LABEL maintainer="Grégory Van den Borre vandenborre.gregory@hotmail.fr"

RUN mkdir /yildiz

RUN curl

ENTRYPOINT ["java", "-jar", "/authentication-server-jar-with-dependencies.jar", "/yildiz/authentication.properties"]

ADD target/authentication-server-jar-with-dependencies.jar /authentication-server-jar-with-dependencies.jar