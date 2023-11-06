package dk.dbc.neptun.rest;

import dk.dbc.neptun.AuthenticatorBean;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.Set;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 */
@ApplicationPath("/neptun")
public class Neptun extends Application {
    private static final Set<Class<?>> CLASSES = Set.of(AuthenticatorBean.class, JacksonFeature.class);

    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }
}
