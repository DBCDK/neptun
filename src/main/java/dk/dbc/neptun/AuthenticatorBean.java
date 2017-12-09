package dk.dbc.neptun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("")
public class AuthenticatorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuthenticatorBean.class);

    public static final String AUTHENTICATE = "authenticate";

    @Resource(lookup = "java:app/env/url/forsrights")
    private String FORSRIGHTS_ENDPOINT;

    @EJB ForsRightsConnectorBean forsRightsConnectorBean;

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
    @Path(AUTHENTICATE)
    public Response authenticate(String authDataXml) {
        try {
            AuthTriple authTriple = forsRightsConnectorBean
                .parseAuthXml(authDataXml);
            boolean authorized = forsRightsConnectorBean
                    .isUserAuthorized(FORSRIGHTS_ENDPOINT,
                    authTriple.getUser(), authTriple.getGroup(),
                    authTriple.getPassword());
            if(authorized) {
                return Response.ok().build();
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
