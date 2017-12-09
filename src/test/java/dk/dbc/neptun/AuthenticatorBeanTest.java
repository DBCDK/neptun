package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

public class AuthenticatorBeanTest extends AbstractForsRightsConnectorTest {
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
}
