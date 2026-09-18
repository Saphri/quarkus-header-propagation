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
                // that call ran on a Mutiny worker thread (emitOn).
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
                .body(containsString("callerThread=executor-thread-1"));
    }

    @Test
    void helloWorldReturnsGreeting() {
        given()
            .header("x-request-id", "req-abc")
            .queryParam("message", "there")
            .when().get("/flow/hello")
            .then()
                .statusCode(200)
                .body(is("hello world"));
    }
}
