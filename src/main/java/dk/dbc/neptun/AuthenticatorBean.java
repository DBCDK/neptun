package dk.dbc.neptun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("")
public class AuthenticatorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuthenticatorBean.class);

    public static final String AUTHENTICATE = "authenticate/version/{version}";

    @Resource(lookup = "java:app/env/url/forsrights")
    private String FORSRIGHTS_ENDPOINT;

    @EJB ForsRightsConnectorBean forsRightsConnectorBean;
    @EJB ConfigFilesHandlerBean configFilesHandlerBean;

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
                    .isUserAuthorized(FORSRIGHTS_ENDPOINT,
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
}
