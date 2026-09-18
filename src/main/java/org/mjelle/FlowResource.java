package org.mjelle;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/**
 * Demonstrates propagating the {@code x-request-id} header to an asynchronous REST client
 * call that runs off the request thread. The id is also stored in the MDC for log correlation,
 * and the call itself is delegated to {@link EchoInvoker}, which executes it on a
 * context-propagating {@code ManagedExecutor}.
 */
@Path("/flow")
public class FlowResource {

    private final Logger log = Logger.getLogger(FlowResource.class);

    @Inject
    EchoInvoker echoInvoker;

    /**
     * Starts a reactive flow: puts the request id in the MDC for log correlation, then delegates
     * to {@link EchoInvoker#invokeEcho(String)}, which performs the asynchronous REST client call
     * off the request thread. The {@code x-request-id} header is propagated to that call
     * automatically (see {@link EchoClient}).
     */
    @GET
    @Path("/start")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> startFlow(
            @HeaderParam("x-request-id") String requestId,
            @QueryParam("name") String name) {
        // item(requestId) -> MDC.put + log -> replaceWith(name) -> flatMap(echoInvoker.invokeEcho)
        // -> log -> map(response). The REST client call happens inside EchoInvoker on a
        // ManagedExecutor thread; x-request-id is propagated to it automatically (see EchoClient).
        return Uni.createFrom().item(requestId)
                .invoke(ri -> MDC.put("x-request-id", ri))
                .invoke(ri -> log.infof("startFlow: requestId=%s",
                        ri))
                .replaceWith(name)
                .flatMap(n -> echoInvoker.invokeEcho(n))
                .invoke(p -> log.infof("%s: returned: %s", requestId, p))
                .map(p -> "callerThread=" + Thread.currentThread().getName()
                        + ", downstream=[" + p + "]");
    }
}
