FROM docker.dbc.dk/payara5-micro

ENV CONFIG_DIR /opt/payara5/deployments/config-files/master

COPY target/neptun*.war app.json deployments/

EXPOSE 8080

LABEL CONFIG_DIR="The config folder"
LABEL IDP_SERVICE_URL="URL to the IDP service"
