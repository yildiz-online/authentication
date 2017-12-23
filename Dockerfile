FROM openjdk:8-jre

LABEL maintainer="Grégory Van den Borre vandenborre.gregory@hotmail.fr"

RUN mkdir /yildiz

RUN sleep 3m

RUN curl -O https://bitbucket.org/yildiz-engine-team/authentication/downloads/authentication-server-jar-with-dependencies.jar

ENTRYPOINT ["java", "-jar", "/authentication-server-jar-with-dependencies.jar", "/yildiz/authentication.properties"]