FROM docker.dbc.dk/payara5-micro

ENV CONFIG_DIR /opt/payara5/deployments/config-files/master

COPY target/neptun*.war app.json deployments/

COPY config-files deployments/config-files

EXPOSE 8080

LABEL SMAUG_URL="URL to the smaug service"
LABEL SMAUG_CLIENT_ID="Smaug client id"
LABEL SMAUG_CLIENT_SECRET="Smaut client secret"
LABEL CONFIG_DIR="The config folder"
LABEL FORSRIGHTS_ENDPOINT="URL to the forsrights service"