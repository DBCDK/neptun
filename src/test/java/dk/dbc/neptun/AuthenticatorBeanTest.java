package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticatorBeanTest extends AbstractForsRightsConnectorTest {
    @Test
    public void test_authenticateOK() throws ConfigFilesHandlerException {
        when(mockedForsRightsService.getForsRightsPort()).thenReturn(
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
        when(mockedForsRightsService.getForsRightsPort()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));
        final String authDataXml = "<blah></ok>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml, 10);

        assertThat("response 500 server error", response.getStatus(), is(500));
    }

    @Test
    public void test_authenticateUnauthorised() throws ConfigFilesHandlerException {
        when(mockedForsRightsService.getForsRightsPort()).thenReturn(
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

    @Test
    public void test_parseAuthHeader() throws ConfigFilesHandlerException, AuthParseException {
        final String base64Auth = "c3BvbmdlYm9iOmJhcm5hY2xlcw==";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final AuthenticatorBean.AuthTuple authTuple =
            authenticatorBean.parseAuthHeader(base64Auth);
        assertThat("username", authTuple.getUsername(), is("spongebob"));
        assertThat("password", authTuple.getPassword(), is("barnacles"));
    }

    @Test
    public void test_parseAuthHeaderInvalidData() throws ConfigFilesHandlerException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String invalidBase64 = "MOOOO\u1f42e";
        assertThrows(AuthParseException.class,
            () -> authenticatorBean.parseAuthHeader(invalidBase64));
    }

    @Test
    public void test_parseAuthHeaderColonlessData() throws ConfigFilesHandlerException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String colonlessBase64 = "c3BvbmdlYm9iCg==";
        assertThrows(AuthParseException.class,
            () -> authenticatorBean.parseAuthHeader(colonlessBase64));
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
