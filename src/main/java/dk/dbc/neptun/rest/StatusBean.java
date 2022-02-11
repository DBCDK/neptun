package dk.dbc.neptun.rest;

import dk.dbc.serviceutils.ServiceStatus;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

@Stateless
@LocalBean
@Path("")
public class StatusBean implements ServiceStatus {
}
