# Building Open Termhub
Instructions on building this project, running tests, making a docker image, and more.

## Prerequisites/Setup
* Java 17+

### Developer Setup

Edit `src/main/resources/application.properties` and edit the `lucene.index.directory` property (OR set the INDEX_DIR environment variable) to indicate the desired location for the Lucene indexes.  NOTE: Unit tests make use of this directory.

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
