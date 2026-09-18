# quarkus-header-propagation

A proof of concept for **seamless `x-request-id` propagation across threads and async HTTP calls** on Quarkus.

The request id travels end-to-end without being threaded through every method signature:

- **Across thread boundaries** — the downstream REST client call runs off the request thread on a MicroProfile Context Propagation `ManagedExecutor` (`runSubscriptionOn`), yet the header still reaches it.
- **Through async HTTP requests** — both the REST client and the downstream resource return `Uni`, so propagation works through reactive calls, not just blocking ones.
- **Log correlation** — the id is stored in the MDC, so it shows up as `<req-...>` on every log line, on every thread.

Built with Quarkus (REST + REST Client). Learn more at <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-header-propagation-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): Build RESTful web services and APIs using Jakarta REST (formerly JAX-RS)
- REST Client ([guide](https://quarkus.io/guides/rest-client)): Type-safe HTTP client for consuming REST APIs using MicroProfile REST Client

## Endpoints

### `GET /flow/start?name=<name>`

Starts a reactive flow. The incoming `x-request-id` header is propagated to an asynchronous
downstream REST client call that runs **off the request thread**, on a MicroProfile Context
Propagation `ManagedExecutor` (via `runSubscriptionOn` in `EchoInvoker`). The id is also stored
in the MDC, so it appears as `<req-...>` in every log line. The response reports the caller
thread and what the downstream service echoed back (including the propagated `x-request-id`).

```shell script
curl -H "x-request-id: req-123" "http://localhost:8080/flow/start?name=quarkus"
# callerThread=vert.x-eventloop-thread-1, downstream=[requestId=req-123, name=quarkus]
```

> The header is propagated automatically by MicroProfile REST Client header propagation
> (`@RegisterClientHeaders` + `org.eclipse.microprofile.rest.client.propagateHeaders=x-request-id`).
> It survives the move off the request thread because the call runs on a MicroProfile Context
> Propagation `ManagedExecutor`, which carries the request context. The MDC entry also keeps the
> id visible in logs across threads.

### `GET /downstream/echo?name=<name>`

Local downstream service used to verify propagation; echoes back the `x-request-id` header
and `name` it received.
