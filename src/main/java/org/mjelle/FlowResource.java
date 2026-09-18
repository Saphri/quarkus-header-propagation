package org.mjelle;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * Demonstrates propagating the {@code x-request-id} header to a REST client call that is
 * executed off the request thread, on Mutiny's default executor (switched via {@code emitOn}).
 */
@Path("/flow")
public class FlowResource {

    private final Logger log = Logger.getLogger(FlowResource.class);

    @Inject
    @RestClient
    EchoClient echoClient;

    /**
     * Starts a reactive flow: the REST client call is made inside the {@code map} step, which
     * runs on Mutiny's default executor after {@code emitOn}. The incoming {@code x-request-id}
     * header is propagated to that client call automatically (see {@link EchoClient}).
     */
    @GET
    @Path("/start")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> startFlow(
            @HeaderParam("x-request-id") String requestId,
            @QueryParam("name") String name) {
        log.infof("startFlow: requestId=%s, name=%s, callerThread=%s",
                requestId, name, Thread.currentThread().getName());
        // item(name) -> emitOn(default executor) -> map(...): the REST client call inside map()
        // runs on Mutiny's default executor, and x-request-id is propagated to it automatically.
        return Uni.createFrom().item(name)
                .emitOn(Infrastructure.getDefaultExecutor())
                .map(n -> "callerThread=" + Thread.currentThread().getName()
                        + ", downstream=[" + echoClient.echo(n) + "]");
    }

    /**
     * Simple greeting endpoint that accepts the {@code x-request-id} header and a message.
     */
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(
            @HeaderParam("x-request-id") String requestId,
            @QueryParam("message") String message) {
        return "hello world";
    }
}
