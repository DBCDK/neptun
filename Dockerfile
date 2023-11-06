FROM docker-dbc.artifacts.dbccloud.dk/payara6-micro:latest

ENV CONFIG_DIR /opt/payara5/deployments/config-files/master

COPY target/neptun*.war app.json deployments/

EXPOSE 8080

LABEL CONFIG_DIR="The config folder"
LABEL IDP_SERVICE_URL="URL to the identity provider service"
LABEL IDP_CACHE_AGE="Amount of time in hours to cache the results from the identity provider service"
