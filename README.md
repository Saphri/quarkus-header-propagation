# quarkus-header-propagation

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

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
- SmallRye Context Propagation ([guide](https://quarkus.io/guides/context-propagation)): Propagate contexts between managed threads in reactive applications

## Endpoints

### `GET /flow/start?name=<name>`

Starts a reactive flow. The incoming `x-request-id` header is propagated to a downstream
REST client call that runs **off the request thread**, on Mutiny's default executor (via
`emitOn`). The response reports which thread made the call and what the downstream service
echoed back (including the propagated `x-request-id`).

```shell script
curl -H "x-request-id: req-123" "http://localhost:8080/flow/start?name=quarkus"
# callerThread=executor-thread-1, downstream=[requestId=req-123, name=quarkus]
```

> The header is propagated automatically by MicroProfile REST Client header propagation
> (`@RegisterClientHeaders` + `org.eclipse.microprofile.rest.client.propagateHeaders=x-request-id`).
> It survives the move off the request thread because Mutiny's default executor participates
> in context propagation.

### `GET /flow/hello?message=<message>`

Accepts the `x-request-id` header and a message, returning `hello world`.

```shell script
curl -H "x-request-id: req-456" "http://localhost:8080/flow/hello?message=there"
# hello world
```

### `GET /downstream/echo?name=<name>`

Local downstream service used to verify propagation; echoes back the `x-request-id` header
and `name` it received.
