package dk.dbc.neptun;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.httpclient.HttpPost;
import net.jodah.failsafe.RetryPolicy;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Stateless
public class SmaugConnectorBean {
    @Resource(lookup = "java:app/env/url/smaug")
    private String SMAUG_URL;

    @Resource(lookup = "java:app/env/smaugclientid")
    private String SMAUG_CLIENT_ID;

    @Resource(lookup = "java:app/env/smaugclientsecret")
    private String SMAUG_CLIENT_SECRET;

    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
        .retryOn(Collections.singletonList(ProcessingException.class))
        .retryIf((Response response) -> response.getStatus() == 404 ||
            response.getStatus() == 500 || response.getStatus() == 502)
        .withDelay(10, TimeUnit.SECONDS)
        .withMaxRetries(6);

    /**
     * Authenticates a user via smaug
     * @param username username
     * @param password password
     * @return true if user is authenticated
     */
    public boolean authenticate(String username, String password) {
        final Client client = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
        final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(
            client, RETRY_POLICY);
        final String base64EncodedAuth = base64EncodeAuth(SMAUG_CLIENT_ID,
            SMAUG_CLIENT_SECRET);
        final Response response = new HttpPost(failSafeHttpClient)
            .withBaseUrl(SMAUG_URL + "/oauth/token")
            .withHeader("Authorization", String.format("Basic %s",
                base64EncodedAuth))
            .withData(String.format(
                "grant_type=password&username=%s&password=%s", username,
                password), "application/x-www-form-urlencoded")
            .execute();
        try {
            return Response.Status.fromStatusCode(response.getStatus())
                == Response.Status.OK;
        } finally {
            response.close();
        }
    }

    protected String base64EncodeAuth(String username, String password) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(String.format("%s:%s", username,
            password).getBytes(StandardCharsets.UTF_8));
    }
}
