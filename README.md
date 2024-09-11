# open-terminology-service
Open source FHIR terminology service deployable as a simple docker container



## Prerequisites/Setup
* Java 17
* Set up a ~/.gradle/gradle.properties file


`mkdir ~/.gradle`

`cat > ~/.gradle/gradle.properties << EOF`

`nexusUsername=...nexus username...`

`nexusPassword=...nexus password...`

`EOF`



### Developer Setup

Edit application.properties and to indicate the location of the index on the system.  Unit tests use the build/index directory.


### Running Unit Tests


All tests

`./gradlew test`


Individual Test for Concept

`./gradlew test --tests ConceptUnitTest`



Use ConceptLoader to load concepts in JSON format.

`./gradlew runLoadConcepts -PinputFile="YOUR_CONCEPTS_JSON_FILE"`


### Building, testing
* To clean use `make clean`
* To build use `make build`
* To test use `make test`


Developer TODOs:
- [ ] Solve duplicate index name for concept term name and other field in repeating Objects for concept.
- [ ] Search by name, id.
- [ ] Verify paging results.
