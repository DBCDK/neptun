package dk.dbc.neptun;

import dk.dbc.forsrights.service.ErrorType;
import dk.dbc.forsrights.service.ForsRightsPortType;
import dk.dbc.forsrights.service.ForsRightsRequest;
import dk.dbc.forsrights.service.ForsRightsResponse;
import dk.dbc.forsrights.service.ForsRightsService;
import dk.dbc.forsrights.service.Ressource;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticatorBeanTest {

    private ForsRightsService mockedForsRightsService = mock(
        ForsRightsService.class);

    @Test
    public void test_authenticateOK() {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));

        final String authDataXml = "<authTriple>" +
            "<user>anastasia</user>" +
            "<group>steele</group>" +
            "<password>inn3r_g0dess</password>" +
            "</authTriple>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml);

        assertThat("response 200 OK", response.getStatus(), is(200));
    }

    @Test
    public void test_authenticateInvalidXml() {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));
        final String authDataXml = "<blah></ok>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml);

        assertThat("response 500 server error", response.getStatus(), is(500));
    }

    @Test
    public void test_authenticateUnauthorised() {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseUnauthorised()));
        final String authDataXml = "<authTriple>" +
            "<user>eugene</user>" +
            "<group>krabs</group>" +
            "<password>hunter2</password>" +
            "</authTriple>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml);

        assertThat("response 401 unauthorised", response.getStatus(), is(401));
    }

    private AuthenticatorBean getAuthenticatorBean() {
        final AuthenticatorBean authenticatorBean = new AuthenticatorBean();
        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        forsRightsConnectorBean.service = mockedForsRightsService;
        authenticatorBean.forsRightsConnectorBean = forsRightsConnectorBean;
        return authenticatorBean;
    }

    private ForsRightsResponse getForsRightsResponseOK() {
        MockedForsRightsResponse response = new MockedForsRightsResponse();
        response.setError(null);
        response.setResources(Collections.singletonList(new Ressource()));
        return response;
    }

    private ForsRightsResponse getForsRightsResponseUnauthorised() {
        MockedForsRightsResponse response = new MockedForsRightsResponse();
        response.setError(ErrorType.AUTHENTICATION_ERROR);
        response.setResources(null);
        return response;
    }


    private final class MockedForsRightsResponse extends ForsRightsResponse {
        public void setResources(List<Ressource> resources) {
            this.ressource = resources;
        }
    }

    private final class MockedForsRightsPort implements ForsRightsPortType, BindingProvider {
        private ForsRightsResponse response;

        public MockedForsRightsPort(ForsRightsResponse response) {
            this.response = response;
        }

        @Override
        public ForsRightsResponse forsRights(ForsRightsRequest request) {
            return response;
        }

        @Override
        public Map<String, Object> getResponseContext() {
            return null;
        }

        @Override
        public Map<String, Object> getRequestContext() {
            return new HashMap<>(1);
        }

        @Override
        public EndpointReference getEndpointReference() {
            return null;
        }

        @Override
        public <T extends EndpointReference> T getEndpointReference(Class<T> aClass) {
            return null;
        }

        @Override
        public Binding getBinding() {
            return null;
        }
    }
}
