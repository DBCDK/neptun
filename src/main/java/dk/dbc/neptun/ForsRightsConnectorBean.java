package dk.dbc.neptun;

import dk.dbc.forsrights.service.ErrorType;
import dk.dbc.forsrights.service.ForsRightsPortType;
import dk.dbc.forsrights.service.ForsRightsRequest;
import dk.dbc.forsrights.service.ForsRightsResponse;
import dk.dbc.forsrights.service.ForsRightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.BindingProvider;
import java.io.StringReader;

@Stateless
@Path("")
public class ForsRightsConnectorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        ForsRightsConnectorBean.class);

    @Resource(lookup = "java:app/env/url/forsrights")
    private String FORSRIGHTS_ENDPOINT;

    protected ForsRightsService service;

    public ForsRightsConnectorBean() {
        // instantiated here to accommodate overriding it when testing
        service = new ForsRightsService();
    }

    private boolean isUserAuthorized(String baseUrl, String user,
            String group, String password) throws ForsRightsConnectorException {
        ForsRightsPortType port = service.getForsRightsPortType();
        BindingProvider bindingProvider =
            (BindingProvider) port;
        bindingProvider.getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseUrl);

        ForsRightsRequest request = new ForsRightsRequest();
        request.setUserIdAut(user);
        request.setPasswordAut(password);
        request.setGroupIdAut(group);

        ForsRightsResponse response = port.forsRights(request);
        ErrorType error = response.getError();
        if(error != null) {
            if(error == ErrorType.AUTHENTICATION_ERROR ||
                    error == ErrorType.USER_NOT_FOUND) {
                return false;
            } else {
                throw new ForsRightsConnectorException(String.format(
                    "error authenticating user/group: %s/%s - %s", user,
                    group, error.value()));
            }
        } else {
            return response.getRessource().size() > 0;
        }
    }

    private AuthTriple parseAuthXml(String authXml) throws ForsRightsConnectorException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AuthTriple.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (AuthTriple) unmarshaller.unmarshal(new StringReader(
                authXml));
        } catch (JAXBException e) {
            throw new ForsRightsConnectorException(e);
        }
    }

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
    @Path("authenticate")
    public Response authenticate(String authDataXml) {
        try {
            AuthTriple authTriple = parseAuthXml(authDataXml);
            boolean authorized = isUserAuthorized(FORSRIGHTS_ENDPOINT,
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
