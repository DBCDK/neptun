/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Stateless
@Path("")
public class AuthenticatorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            AuthenticatorBean.class);

    public static final String AUTHENTICATE = "authenticate/version/{version}";
    public static final String AUTHENTICATE_AD = "authenticate/ad/version/{version}";

    private String FORSRIGHTS_URL = System.getenv().getOrDefault("FORSRIGHTS_URL", "FORSRIGHTS_URL environment variable not set");

    @EJB ForsRightsConnectorBean forsRightsConnectorBean;
    @EJB ConfigFilesHandlerBean configFilesHandlerBean;
    @EJB SmaugConnectorBean smaugConnectorBean;

    /**
     * looks up user rights in forsrights based on a netpunkt triple:
     * userid, groupid, password
     *
     * @param authDataXml xml containing userid, groupid, and password
     * @return 200 OK on authorised users, 401 Unauthorized on unauthorised
     * users and 500 Server Error on backend exceptions
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path(AUTHENTICATE)
    public Response authenticate(String authDataXml, @PathParam("version") int version) {
        try {
            AuthTriple authTriple = forsRightsConnectorBean
                .parseAuthXml(authDataXml);
            boolean authorized = forsRightsConnectorBean
                    .isUserAuthorized(FORSRIGHTS_URL,
                    authTriple.getUser(), authTriple.getGroup(),
                    authTriple.getPassword());
            if(authorized) {
                try {
                    return Response.ok(configFilesHandlerBean.getConfigFiles(version)).build();
                } catch(ConfigFilesHandlerException e) {
                    LOGGER.error("unexpected error when finding config files", e);
                    return Response.serverError().entity(
                        "unexpected error when finding config files").build();
                }
            } else {
                // 401 Unauthorized: auth credentials refused
                return Response.status(401).build();
            }
        } catch (ForsRightsConnectorException e) {
            LOGGER.error("unexpected exception when authorising user", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path(AUTHENTICATE_AD)
    public Response authenticateAD(@Context HttpHeaders headers,
            @PathParam("version") int version) {
        final List<String> authHeader = headers.getRequestHeader(
            HttpHeaders.AUTHORIZATION);
        if(authHeader == null || authHeader.size() != 1) {
            return Response.serverError().entity(
                "missing authorization header").build();
        }
        if(authHeader.get(0).length() < 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                "malformed authorization header").build();
        }
        try {
            // take substring 6 to get data after "basic "
            final AuthTuple auth = parseAuthHeader(authHeader.get(0)
                .substring(6));
            final boolean authenticated = smaugConnectorBean.authenticate(
                auth.getUsername(), auth.getPassword());
            if(authenticated) {
                try {
                    return Response.ok(configFilesHandlerBean.getConfigFiles(version)).build();
                } catch(ConfigFilesHandlerException e) {
                    LOGGER.error("unexpected error when finding config files", e);
                    return Response.serverError().entity(
                        "unexpected error when finding config files").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (AuthParseException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    protected AuthTuple parseAuthHeader(String auth) throws AuthParseException {
        try {
            final byte[] authBytes = DatatypeConverter.parseBase64Binary(auth);
            final String parsedAuth = new String(authBytes,
                StandardCharsets.UTF_8);
            final String[] parts = parsedAuth.split(":");
            if(parts.length != 2) {
                throw new AuthParseException(String.format("unable to split %s",
                    parsedAuth));
            }
            final String username = parts[0];
            final String password = parts[1];
            return new AuthTuple(username, password);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new AuthParseException(String.format("unable to parse %s",
                auth), e);
        }
    }

    static class AuthTuple {
        private String username;
        private String password;
        public AuthTuple(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
