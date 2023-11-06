package dk.dbc.neptun;

import dk.dbc.idp.connector.IDPConnector;
import dk.dbc.idp.connector.IDPConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXB;
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
     * Looks up user rights in identity provider service based on a netpunkt triple:
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
