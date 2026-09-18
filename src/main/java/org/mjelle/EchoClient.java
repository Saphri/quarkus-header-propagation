package org.mjelle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

/**
 * REST client for the local {@link EchoResource} downstream service. The method returns a
 * {@code Uni} so the call is asynchronous.
 *
 * <p>The {@code x-request-id} header is not declared on the method: it is propagated
 * automatically by MicroProfile REST Client header propagation -- {@code @RegisterClientHeaders}
 * here plus the {@code org.eclipse.microprofile.rest.client.propagateHeaders=x-request-id}
 * setting. This still works when the call runs off the request thread (see {@link EchoInvoker}),
 * because it executes on a MicroProfile Context Propagation {@code ManagedExecutor} that carries
 * the request context.
 */
@RegisterClientHeaders
@RegisterRestClient(configKey = "echo")
public interface EchoClient {

    @GET
    @Path("/downstream/echo")
    Uni<String> echo(@QueryParam("name") String name);
}
