package org.mjelle;

import org.jboss.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Simulates a downstream service. It echoes back the {@code x-request-id} header and
 * the {@code name} query parameter it received, so tests can assert that the header
 * was propagated to the REST client call.
 */
@Path("/downstream")
public class EchoResource {

    private final Logger log = Logger.getLogger(EchoResource.class);

    @GET
    @Path("/echo")
    @Produces(MediaType.TEXT_PLAIN)
    public String echo(
            @HeaderParam("x-request-id") String requestId,
            @QueryParam("name") String name) {
        log.infof("startFlow: requestId=%s, name=%s, callerThread=%s",
                requestId, name, Thread.currentThread().getName());
        return "requestId=" + (requestId == null ? "<none>" : requestId)
                + ", name=" + (name == null ? "<none>" : name);
    }
}
