package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetpunktParserTest {

    @Test
    void test_parseAuthXml() throws JAXBException {
        final String authDataXml = "<authTriple>" +
                "<user>patrick</user>" +
                "<group>star</group>" +
                "<password>barnacles</password>" +
                "</authTriple>";
        final AuthTriple result = NetpunktParser.parseAuthXml(authDataXml);

        assertThat("user", result.getUser(), is("patrick"));
        assertThat("group", result.getGroup(), is("star"));
        assertThat("password", result.getPassword(), is("barnacles"));
    }

    @Test
    void test_parseAuthXmlInvalidXml() {
        final String authDataXml = "<blah></ok>";

        assertThrows(JAXBException.class,
                () -> NetpunktParser.parseAuthXml(authDataXml));
    }

}
