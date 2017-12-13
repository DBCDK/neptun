FROM docker.dbc.dk/payara-micro

USER root
RUN apt-get update && apt-get install -y subversion zip

ENV USER gfish
USER $USER

COPY target/neptun.war wars

# path for config directory should not be configurable on build or run time
RUN svn export --non-interactive --trust-server-cert https://svn.dbc.dk/repos/dbckat_datasetup/trunk/ /payara-micro/dbckat.d
# temporarily make a version 1 directory until the svn project is restructured
RUN mkdir 1 && mv /payara-micro/dbckat.d/* 1 && mv 1 /payara-micro/dbckat.d

# a version with find -execdir would likely be nicer but would create a zip
# with a diretory with all the files instead of having all the files (and
# subdirs) in the root of the zip file
RUN cd /payara-micro/dbckat.d && \
	for d in *; do \
		test -d $d && cd $d && zip -r ../$(basename $d).zip * && rm -r ../$d; \
	done

EXPOSE 8080
