package dk.dbc.neptun.rest;

import dk.dbc.neptun.AuthenticatorBean;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 */
@ApplicationPath("/neptun")
public class Neptun extends Application {
    private static final Set<Class<?>> CLASSES = new HashSet<>(asList(
            AuthenticatorBean.class, JacksonFeature.class
    ));

    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }
}
