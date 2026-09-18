package org.mjelle;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FlowResourceTest {

    @Test
    void startFlowPropagatesRequestIdToRestClientCall() {
        String requestId = "req-12345";
        given()
            .header("x-request-id", requestId)
            .queryParam("name", "quarkus")
            .when().get("/flow/start")
            .then()
                .statusCode(200)
                // The downstream service echoes back the header it received; asserting it
                // here proves x-request-id was propagated to the REST client call, even though
                // that call ran off the request thread (on a ManagedExecutor, see EchoInvoker).
                .body(containsString("requestId=" + requestId))
                .body(containsString("name=quarkus"));
    }

    @Test
    void startFlowRunsRestClientCallOnWorkerThread() {
        given()
            .header("x-request-id", "req-thread")
            .queryParam("name", "quarkus")
            .when().get("/flow/start")
            .then()
                .statusCode(200)
                // The call must NOT run on the Vert.x event-loop thread.
                .body(containsString("callerThread=vert.x-eventloop-thread"));
    }
}
