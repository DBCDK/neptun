package dk.dbc.neptun;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


/**
 * Temporary dummy status endpoint. DBCkat has a hard coded reference to /neptun/status so until that is fixed we need
 * a /neptun/status endpoint which simply returns 200 OK
 */
@Stateless
@Path("/")
public class Status {

    @GET
    @Path("status")
    public Response status() {
        return Response.ok().build();
    }
}
