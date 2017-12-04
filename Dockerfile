FROM docker.dbc.dk/payara-micro

COPY target/neptun.war wars

EXPOSE 8080
