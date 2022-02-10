package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SmaugConnectorBeanTest {
    @Test
    void test_base64EncodeAuth() {
        final SmaugConnectorBean smaugConnectorBean = new SmaugConnectorBean();
        final String username = "spongebob";
        final String password = "barnacles";
        final String expected = "c3BvbmdlYm9iOmJhcm5hY2xlcw==";
        final String result = smaugConnectorBean.base64EncodeAuth(username, password);

        assertThat(result, is(expected));
    }
}
