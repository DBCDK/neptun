/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

import dk.dbc.forsrights.service.ErrorType;
import dk.dbc.forsrights.service.ForsRightsPortType;
import dk.dbc.forsrights.service.ForsRightsRequest;
import dk.dbc.forsrights.service.ForsRightsResponse;
import dk.dbc.forsrights.service.ForsRightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.BindingProvider;
import java.io.StringReader;

@Stateless
public class ForsRightsConnectorBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            ForsRightsConnectorBean.class);

    protected ForsRightsService service;

    public ForsRightsConnectorBean() {
        // instantiated here to accommodate overriding it when testing
        service = new ForsRightsService();
    }

    public boolean isUserAuthorized(String baseUrl, String user,
                                    String group, String password) throws ForsRightsConnectorException {
        if (baseUrl != null && !baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
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
        if (error != null) {
            if (error == ErrorType.AUTHENTICATION_ERROR ||
                    error == ErrorType.USER_NOT_FOUND) {
                LOGGER.info("authentication failed for " +
                                "user/group {}/{} with error {}", user, group,
                        error.value());
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

    public AuthTriple parseAuthXml(String authXml) throws ForsRightsConnectorException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AuthTriple.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (AuthTriple) unmarshaller.unmarshal(new StringReader(
                    authXml));
        } catch (JAXBException e) {
            throw new ForsRightsConnectorException(e);
        }
    }
}
