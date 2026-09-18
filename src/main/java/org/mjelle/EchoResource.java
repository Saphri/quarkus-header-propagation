package org.mjelle;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Simulates a downstream service. It echoes back the {@code x-request-id} header and
 * the {@code name} query parameter it received, so tests can assert that the header
 * was propagated to the REST client call. The id is also stored in the MDC for log
 * correlation, mirroring what the caller does.
 */
@Path("/downstream")
public class EchoResource {

    private final Logger log = Logger.getLogger(EchoResource.class);

    @GET
    @Path("/echo")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> echo(
            @HeaderParam("x-request-id") String requestId,
            @QueryParam("name") String name) {
        return Uni.createFrom().item(requestId)
                .invoke(ri -> MDC.put("x-request-id", ri))
                .invoke(ri -> log.infof("echo: requestId=%s", ri))
                .map(ri -> "requestId=" + (requestId == null ? "<none>" : requestId))
                .map(r -> r + ", name=" + (name == null ? "<none>" : name));
    }
}
