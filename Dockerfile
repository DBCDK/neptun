FROM docker.dbc.dk/payara-micro

USER root
RUN apt-get update && apt-get install -y zip

ENV CONFIG_DIR /payara-micro/dbckat.d/master
ENV USER gfish
USER $USER

COPY build/libs/neptun.war wars

COPY config-files /payara-micro/dbckat.d

EXPOSE 8080
