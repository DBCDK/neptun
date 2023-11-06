package dk.dbc.neptun;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class HealthChecks {

    @Produces
    @Readiness
    public HealthCheck databaseLookup() {
        return () -> HealthCheckResponse.named("status")
                .status(true)
                .build();
    }
}

