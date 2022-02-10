package dk.dbc.neptun;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public class NetpunktParser {

    public static AuthTriple parseAuthXml(String authXml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AuthTriple.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (AuthTriple) unmarshaller.unmarshal(new StringReader(authXml));
    }
}
