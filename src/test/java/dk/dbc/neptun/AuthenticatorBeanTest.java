package dk.dbc.neptun;

import dk.dbc.idp.connector.IDPConnector;
import dk.dbc.idp.connector.IDPConnectorException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatorBeanTest {

    @Mock
    IDPConnector idpConnector = mock(IDPConnector.class);

    @Mock
    ConfigFilesHandlerBean configFilesHandlerBean = mock(ConfigFilesHandlerBean.class);

    @Test
    void test_authenticateOK() throws ConfigFilesHandlerException, IDPConnectorException {
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
    void test_authenticateInvalidXml() throws ConfigFilesHandlerException, IDPConnectorException {
        final String authDataXml = "<blah></ok>";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        Response response = authenticatorBean.authenticate(authDataXml, 10);

        assertThat("response 500 server error", response.getStatus(), is(500));
    }

    @Test
    void test_authenticateUnauthorised() throws ConfigFilesHandlerException, IDPConnectorException {
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
    void test_parseAuthHeader() throws ConfigFilesHandlerException, AuthParseException, IDPConnectorException {
        final String base64Auth = "c3BvbmdlYm9iOmJhcm5hY2xlcw==";
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final AuthenticatorBean.AuthTuple authTuple =
                authenticatorBean.parseAuthHeader(base64Auth);
        assertThat("username", authTuple.getUsername(), is("spongebob"));
        assertThat("password", authTuple.getPassword(), is("barnacles"));
    }

    @Test
    void test_parseAuthHeaderInvalidData() throws ConfigFilesHandlerException, IDPConnectorException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String invalidBase64 = "MOOOO\u1f42e";
        assertThrows(AuthParseException.class,
                () -> authenticatorBean.parseAuthHeader(invalidBase64));
    }

    @Test
    void test_parseAuthHeaderColonlessData() throws ConfigFilesHandlerException, IDPConnectorException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String colonlessBase64 = "c3BvbmdlYm9iCg==";
        assertThrows(AuthParseException.class,
                () -> authenticatorBean.parseAuthHeader(colonlessBase64));
    }

    private AuthenticatorBean getAuthenticatorBean() throws ConfigFilesHandlerException, IDPConnectorException {
        when(configFilesHandlerBean.getConfigFiles(anyInt())).thenReturn(null);
        when(idpConnector.authenticate(anyString(), anyString(), anyString())).thenReturn(false);
        when(idpConnector.authenticate("anastasia", "steele", "inn3r_g0dess")).thenReturn(true);

        final AuthenticatorBean authenticatorBean = new AuthenticatorBean();
        authenticatorBean.idpConnector = this.idpConnector;
        authenticatorBean.configFilesHandlerBean = configFilesHandlerBean;
        return authenticatorBean;
    }
}
