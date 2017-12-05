package dk.dbc.neptun;

import dk.dbc.forsrights.service.ErrorType;
import dk.dbc.forsrights.service.ForsRightsPortType;
import dk.dbc.forsrights.service.ForsRightsRequest;
import dk.dbc.forsrights.service.ForsRightsResponse;
import dk.dbc.forsrights.service.ForsRightsService;

import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.xml.ws.BindingProvider;

@Stateless
@Path("")
public class ForsRightsConnectorBean {
    private boolean isUserAuthorized(String baseUrl, String user,
            String group, String password) throws ForsRightsConnectorException {
        ForsRightsService service = new ForsRightsService();
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
}
