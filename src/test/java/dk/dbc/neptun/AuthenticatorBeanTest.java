package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticatorBeanTest extends AbstractForsRightsConnectorTest {
    @Test
    public void test_authenticateOK() throws ConfigFilesHandlerException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));

        final String authDataXml = "<authTriple>" +
            "<user>anastasia</user>" +
            "<group>steele</group>" +
            "<password>inn3r_g0dess</password>" +
            "</authTriple>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml, 10);

        assertThat("response 200 OK", response.getStatus(), is(200));
    }

    @Test
    public void test_authenticateInvalidXml() throws ConfigFilesHandlerException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));
        final String authDataXml = "<blah></ok>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml, 10);

        assertThat("response 500 server error", response.getStatus(), is(500));
    }

    @Test
    public void test_authenticateUnauthorised() throws ConfigFilesHandlerException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseUnauthorised()));
        final String authDataXml = "<authTriple>" +
            "<user>eugene</user>" +
            "<group>krabs</group>" +
            "<password>hunter2</password>" +
            "</authTriple>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml, 10);

        assertThat("response 401 unauthorised", response.getStatus(), is(401));
    }

    private AuthenticatorBean getAuthenticatorBean() throws ConfigFilesHandlerException {
        final AuthenticatorBean authenticatorBean = new AuthenticatorBean();
        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        final ConfigFilesHandlerBean configFilesHandlerBean =
            mock(ConfigFilesHandlerBean.class);
        when(configFilesHandlerBean.getConfigFiles(anyInt())).thenReturn(
            mock(File.class));
        forsRightsConnectorBean.service = mockedForsRightsService;
        authenticatorBean.forsRightsConnectorBean = forsRightsConnectorBean;
        authenticatorBean.configFilesHandlerBean = configFilesHandlerBean;
        return authenticatorBean;
    }
}
