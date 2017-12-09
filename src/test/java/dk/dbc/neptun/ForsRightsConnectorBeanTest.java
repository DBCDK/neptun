package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ForsRightsConnectorBeanTest extends AbstractForsRightsConnectorTest {
    @Test
    public void test_parseAuthXml() throws ForsRightsConnectorException {
        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        final String authDataXml = "<authTriple>" +
            "<user>patrick</user>" +
            "<group>star</group>" +
            "<password>barnacles</password>" +
            "</authTriple>";
        final AuthTriple result = forsRightsConnectorBean.parseAuthXml(authDataXml);

        assertThat("user", result.getUser(), is("patrick"));
        assertThat("group", result.getGroup(), is("star"));
        assertThat("password", result.getPassword(), is("barnacles"));
    }

    @Test
    public void test_parseAuthXmlInvalidXml() {
        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        final String authDataXml = "<blah></ok>";

        assertThrows(ForsRightsConnectorException.class,
            () -> forsRightsConnectorBean.parseAuthXml(authDataXml));
    }

    @Test
    public void test_isUserAuthorised() throws ForsRightsConnectorException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseOK()));

        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        forsRightsConnectorBean.service = mockedForsRightsService;

        assertThat(forsRightsConnectorBean.isUserAuthorized("", "", "", ""),
            is(true));
    }

    @Test
    public void test_isUserAuthorisedNoRights() throws ForsRightsConnectorException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseNoRights()));

            final ForsRightsConnectorBean forsRightsConnectorBean =
                new ForsRightsConnectorBean();
            forsRightsConnectorBean.service = mockedForsRightsService;

            assertThat(forsRightsConnectorBean.isUserAuthorized("", "", "",
                ""), is(false));
    }

    @Test
    public void test_isUserAuthorisedUnauthorised() throws ForsRightsConnectorException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseUnauthorised()));

        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        forsRightsConnectorBean.service = mockedForsRightsService;

        assertThat(forsRightsConnectorBean.isUserAuthorized("", "", "",
            ""), is(false));
    }

    @Test
    public void test_isUserAuthorisedError() throws ForsRightsConnectorException {
        when(mockedForsRightsService.getForsRightsPortType()).thenReturn(
            new MockedForsRightsPort(getForsRightsResponseError()));

        final ForsRightsConnectorBean forsRightsConnectorBean =
            new ForsRightsConnectorBean();
        forsRightsConnectorBean.service = mockedForsRightsService;

        assertThrows(ForsRightsConnectorException.class,
            () -> forsRightsConnectorBean.isUserAuthorized("", "", "", ""));
    }
}
