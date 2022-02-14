package dk.dbc.neptun;

import dk.dbc.idp.connector.IDPConnector;
import dk.dbc.idp.connector.IDPConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

@Stateless
@Path("")
public class AuthenticatorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatorBean.class);

    @Inject
    IDPConnector idpConnector;

    @EJB
    ConfigFilesHandlerBean configFilesHandlerBean;


    /**
     * looks up user rights in forsrights based on a netpunkt triple:
     * userid, groupid, password
     *
     * @param authDataXml xml containing userid, groupid, and password
     * @param version     the wanted config file version
     * @return 200 OK on authorised users, 400 Bad Request if the auth body can't be parsed,
     * 401 Unauthorized on unauthorized users and 500 Server Error on backend exceptions
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("authenticate/version/{version}")
    public Response authenticate(String authDataXml, @PathParam("version") int version) {
        try {
            final AuthTriple authTriple = parseAuthXml(authDataXml);

            final boolean authenticated = idpConnector.authenticate(authTriple.getUser(),
                    authTriple.getGroup(),
                    authTriple.getPassword());
            if (authenticated) {
                try {
                    return Response.ok(configFilesHandlerBean.getConfigFiles(version)).build();
                } catch (ConfigFilesHandlerException e) {
                    LOGGER.error("unexpected error when finding config files", e);
                    return Response.serverError().entity(
                            "unexpected error when finding config files").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (IDPConnectorException e) {
            LOGGER.error("unexpected exception when authorising user", e);
            return Response.serverError().build();
        } catch (DataBindingException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid XML body").build();
        }
    }

    AuthTriple parseAuthXml(String authXml) throws DataBindingException {
        return JAXB.unmarshal(new StreamSource(new StringReader(authXml)), AuthTriple.class);
    }

}
