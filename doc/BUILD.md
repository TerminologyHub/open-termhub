# Building Open Termhub
Instructions on building this project, running tests, making a docker image, and more.

## Prerequisites/Setup
* Java 17+

### Developer Setup

Edit `src/main/resources/application.properties` and edit the `lucene.index.directory` property (OR set the INDEX_DIR environment variable) to indicate the desired location for the Lucene indexes.  NOTE: Unit tests make use of this directory.

## Syndication configuration

Syndication is disabled unless a token is provided. Configure via properties or environment variables:

- `syndication.token` (or `PROJECT_API_KEY`) — required to enable syndication
- `syndication.check.on-startup` (or `SYNDICATION_CHECK_ON_STARTUP`) — optional; when `true`, performs a one-time syndication load at startup; if not set/false, no startup syndication occurs
- `syndication.check.cron` (or `SYNDICATION_CHECK_CRON`) — optional; a Spring cron expression to enable periodic re-syndication; if not set/empty, no schedule is registered

Example `application.properties` entries:

```
syndication.token=<your-project-api-key>
# Optional one-time startup load
syndication.check.on-startup=true
# Optional periodic re-syndication (Spring cron: sec min hour dom mon dow)
syndication.check.cron=0 0 0 * * *
```

Equivalent environment variables:

```
export PROJECT_API_KEY=<your-project-api-key>
export SYNDICATION_CHECK_ON_STARTUP=true
export SYNDICATION_CHECK_CRON="0 0 0 * * *"
```

### Building, testing, running
* To clean use `make clean`
* To build use `make build`
* To make the docker image use `make docker`
* To test use `make test`
* To run use `make run`
* To run with debug `make debugrun`

To run individual tests, specify the tests, e.g.

`./gradlew test --tests ConceptUnitTest`


### API Documentation (Swagger)

Once you have the service up and running (assuming at http://localhost:8080), find the swagger page at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
