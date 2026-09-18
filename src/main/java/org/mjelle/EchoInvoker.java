package org.mjelle;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped 
/**
 * Performs the downstream REST client call off the request thread. The subscription is run on an
 * injected MicroProfile Context Propagation {@code ManagedExecutor} (via {@code runSubscriptionOn}),
 * so the request context -- and therefore the propagated {@code x-request-id} header -- is carried
 * across to that worker thread.
 */
public class EchoInvoker {

    private final Logger log = Logger.getLogger(EchoInvoker.class);

    @Inject
    ManagedExecutor executor;

    @Inject
    @RestClient
    EchoClient echoClient;

    public Uni<String> invokeEcho(final String name) {
        return Uni.createFrom().item(name)
                .runSubscriptionOn(executor)
                .invoke(n -> log.infof("invokeEcho: name=%s", n))
                .flatMap(n -> echoClient.echo(n));
    }
}
