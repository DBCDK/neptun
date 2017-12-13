FROM docker.dbc.dk/payara-micro

USER root
RUN apt-get update && apt-get install -y subversion zip

# CONFDIR should be an absolute path to make it usable from the
# application server
ENV CONFDIR /payara-micro/dbckat.d
ENV USER gfish
USER $USER

COPY target/neptun.war wars

RUN svn export --non-interactive --trust-server-cert https://svn.dbc.dk/repos/dbckat_datasetup/trunk/ $CONFDIR
# temporarily make a version 1 directory until the svn project is restructured
RUN mkdir 1 && mv $CONFDIR/* 1 && mv 1 $CONFDIR

# a version with find -execdir would likely be nicer but would create a zip
# with a diretory with all the files instead of having all the files (and
# subdirs) in the root of the zip file
RUN cd $CONFDIR && \
	for d in *; do \
		test -d $d && cd $d && zip -r ../$(basename $d).zip * && rm -r ../$d; \
	done

EXPOSE 8080
