package dk.dbc.neptun;

import dk.dbc.idp.connector.IDPConnector;
import dk.dbc.idp.connector.IDPConnectorException;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DataBindingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthenticatorBeanTest {

    @Mock
    IDPConnector idpConnector;

    @Mock
    ConfigFilesHandlerBean configFilesHandlerBean;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

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

        assertThat("response 400 bad request", response.getStatus(), is(400));
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

    private AuthenticatorBean getAuthenticatorBean() throws ConfigFilesHandlerException, IDPConnectorException {
        when(configFilesHandlerBean.getConfigFiles(anyInt())).thenReturn(null);
        when(idpConnector.authenticate(anyString(), anyString(), anyString())).thenReturn(false);
        when(idpConnector.authenticate("anastasia", "steele", "inn3r_g0dess")).thenReturn(true);

        final AuthenticatorBean authenticatorBean = new AuthenticatorBean();
        authenticatorBean.idpConnector = this.idpConnector;
        authenticatorBean.configFilesHandlerBean = configFilesHandlerBean;
        return authenticatorBean;
    }

    @Test
    void test_parseAuthXml() throws ConfigFilesHandlerException, IDPConnectorException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String authDataXml = "<authTriple>" +
                "<user>patrick</user>" +
                "<group>star</group>" +
                "<password>barnacles</password>" +
                "</authTriple>";
        final AuthTriple result = authenticatorBean.parseAuthXml(authDataXml);

        assertThat("user", result.getUser(), is("patrick"));
        assertThat("group", result.getGroup(), is("star"));
        assertThat("password", result.getPassword(), is("barnacles"));
    }

    @Test
    void test_parseAuthXmlInvalidXml() throws ConfigFilesHandlerException, IDPConnectorException {
        final AuthenticatorBean authenticatorBean = getAuthenticatorBean();
        final String authDataXml = "<blah></ok>";

        assertThrows(DataBindingException.class,
                () -> authenticatorBean.parseAuthXml(authDataXml));
    }
}
