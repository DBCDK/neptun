package dk.dbc.neptun;

import dk.dbc.forsrights.service.ErrorType;
import dk.dbc.forsrights.service.ForsRightsPortType;
import dk.dbc.forsrights.service.ForsRightsRequest;
import dk.dbc.forsrights.service.ForsRightsResponse;
import dk.dbc.forsrights.service.ForsRightsService;
import dk.dbc.forsrights.service.Ressource;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class AbstractForsRightsConnectorTest {
    ForsRightsService mockedForsRightsService = mock(
        ForsRightsService.class);

    public ForsRightsResponse getForsRightsResponseOK() {
        final MockedForsRightsResponse response =
            new MockedForsRightsResponse();
        response.setError(null);
        response.setResources(Collections.singletonList(new Ressource()));
        return response;
    }

    public ForsRightsResponse getForsRightsResponseUnauthorised() {
        final MockedForsRightsResponse response =
            new MockedForsRightsResponse();
        response.setError(ErrorType.AUTHENTICATION_ERROR);
        response.setResources(null);
        return response;
    }

    public ForsRightsResponse getForsRightsResponseNoRights() {
        final MockedForsRightsResponse response =
            new MockedForsRightsResponse();
        response.setError(null);
        response.setResources(Collections.emptyList());
        return response;
    }

    public ForsRightsResponse getForsRightsResponseError() {
        final MockedForsRightsResponse response =
            new MockedForsRightsResponse();
        response.setError(ErrorType.SERVICE_UNAVAILABLE);
        response.setResources(null);
        return response;
    }

    public final class MockedForsRightsResponse extends ForsRightsResponse {
        public void setResources(List<Ressource> resources) {
            this.ressource = resources;
        }
    }

    public final class MockedForsRightsPort implements ForsRightsPortType, BindingProvider {
        private ForsRightsResponse response;

        public MockedForsRightsPort(ForsRightsResponse response) {
            this.response = response;
        }

        @Override
        public ForsRightsResponse forsRights(ForsRightsRequest request) {
            return response;
        }

        @Override
        public Map<String, Object> getResponseContext() {
            return null;
        }

        @Override
        public Map<String, Object> getRequestContext() {
            return new HashMap<>(1);
        }

        @Override
        public EndpointReference getEndpointReference() {
            return null;
        }

        @Override
        public <T extends EndpointReference> T getEndpointReference(Class<T> aClass) {
            return null;
        }

        @Override
        public Binding getBinding() {
            return null;
        }
    }
}
