# Step-by-step instructions with Sandbox data
Instructions on using data local to this project to get Open Termhub up and running within 5 minutes.

[Tutorial Training Video (TBD)](TBD)

## Prerequisites/Setup
* Java 17+
* This tutorial assumes you have cloned this open-termhub repository.


## Build and run the server

### Option 1: build/run without docker

One option is to just build the code and run the server locally and use an INDEX_DIR environment variable to specify the directory where Lucene indexes should live (make sure this directory exists)

```
# On Windows use export INDEX_DIR=c:/temp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
export ENABLE_POST_LOAD_COMPUTATIONS=true
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR
make build run
```

### Option 2: build/run with docker

the other option is to build the docker image and run as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists)

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
make docker
docker run -d --rm --name open-termhub \
  -e ENABLE_POST_LOAD_COMPUTATIONS=true \
  -v "$INDEX_DIR":/index -p 8080:8080 wcinformatics/open-termhub:latest
```

### Option 3: run with public docker image

The final option is to run the latest published public docker image as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists):

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
docker run -d --rm --name open-termhub \
  -e ENABLE_POST_LOAD_COMPUTATIONS=true \
  -v "$INDEX_DIR":/index -p 8080:8080 wcinformatics/open-termhub:latest
```

**[Back to top](#step-by-step-instructions-with-sandbox-data)**


## View API Documentation

All three of the above options will yield a running server and you should now you should be able to access the Swagger UI pages:
* Swagger [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* FHIR R4 Swagger [http://localhost:8080/fhir/r4/swagger-ui/index.html](http://localhost:8080/fhir/r4/swagger-ui/index.html)
* FHIR R5 Swagger [http://localhost:8080/fhir/r5/swagger-ui/index.html](http://localhost:8080/fhir/r5/swagger-ui/index.html)

**[Back to top](#step-by-step-instructions-with-sandbox-data)**


## Loading SANDBOX data

The sandbox data is a collection of mini terminology assets derived from the major
vocabularies used in healthcare in the US including SNOMED, LOINC, RXNORM, ICD10.
This data exactly corresponds to the data in the "Sandbox" public project in TermHub
cloud server itself. Copies of these files exist in src/main/resources/data and so 
can be loaded directly from here once the server is running.

Use one of the options above to ensure the server is running and then run the
following curl commands which will load the data from FHIR R5 CodeSystem and ConceptMap
resources.


#### Load Sandbox SNOMEDCT_US

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-snomedct_us-sandbox-20240301-r5.json' | jq
```

#### Load Sandbox RXNORM

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-rxnorm-sandbox-04012024-r5.json' | jq
```

#### Load Sandbox ICD10CM

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-icd10cm-sandbox-2023-r5.json' | jq
```

#### Load Sandbox LNC

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-lnc-sandbox-277-r5.json' | jq
```

#### Load the SNOMEDCT_US to ICD10CM concept maps 

```
curl -X POST http://localhost:8080/fhir/r5/ConceptMap \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json' | jq
```

#### Load the SNOMEDCT_US EXTENSION value set

```
curl -X POST http://localhost:8080/fhir/r5/ValueSet \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/ValueSet-snomedct_us-extension-sandbox-20240301-r5.json' | jq
```

After running the commands above, the running server should be loaded with the
specified data.  The entire runtime for this is about 1 min.

**[Back to top](#step-by-step-instructions-with-sandbox-data)**


## Demonstrating the UI

Now that we have data loaded, we can try a several curl commands to demonstrate
basic function. Alternatively, you can

[![Run in Postman](https://run.pstmn.io/button.svg)](postman-open-termhub.json).

### Testing the Terminology API

The following code block has a number of curl commands that test a few of the terminology API endpoints of the server to demonstrate basic function.

```
# Find terminologies (e.g. code systems)
curl "http://localhost:8080/terminology" | jq

# Find terminology metadata
id=`curl "http://localhost:8080/terminology" | jq -r '.items[0].id'`
curl "http://localhost:8080/terminology/$id/metadata" | jq

# Get a SNOMEDCT concept by code
curl "http://localhost:8080/concept/SNOMEDCT_US/107907001" | jq
curl "http://localhost:8080/concept/SNOMEDCT_US/107907001/relationships" | jq
curl "http://localhost:8080/concept/SNOMEDCT_US/107907001/trees" | jq

# Perform a SNOMEDCT search with a word query
curl "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal" | jq

# Perform a SNOMEDCT search with a code query
curl "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal" | jq

# Perform a SNOMEDCT search with just an ECL expression
curl "http://localhost:8080/concept?terminology=SNOMEDCT_US&expression=%3C%3C128927009&include=minimal" | jq

# Perform a SNOMEDCT search with a query and an ECL expression
curl "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=gastrointestinal&expression=%3C%3C128927009&include=minimal" | jq

# Find mapsets (e.g. concept maps)
curl "http://localhost:8080/mapset" | jq

# Find mappings across all mapsets
curl "http://localhost:8080/mapping" | jq

# Find mapset mappings
curl "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping" | jq

# Find mappings in a particular mapset for a particular "from" code
curl "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005" | jq
curl "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=from.code:300862005" | jq

# Find subsets (e.g. value sets)
curl "http://localhost:8080/subset" | jq

# Find members across all subsets
curl "http://localhost:8080/member" | jq

# Find subset members
curl "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member" | jq

# Find members in a particular subset for a particular code
curl "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes" | jq
curl "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=name:diabetes" | jq

```

### Testing the FHIR R4 API

The following code block has a number of curl commands that test a few of the FHIR R4 API endpoints of the server to demonstrate basic function.

```
# Find CodeSystems
curl 'http://localhost:8080/fhir/r4/CodeSystem' | jq

# Perform a SNOMEDCT CodeSystem $lookup for a code
curl 'http://localhost:8080/fhir/r4/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009' | jq

# Find ConceptMaps
curl 'http://localhost:8080/fhir/r4/ConceptMap' | jq

# Perform a ConceptMap $translate to find "target" codes for a SNOMEDCT code
curl 'http://localhost:8080/fhir/r4/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106&system=http://snomed.info/sct&code=300862005' | jq

# Find implied ValueSets for CodeSystems and explicit value sets
curl 'http://localhost:8080/fhir/r4/ValueSet' | jq
curl 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs' | jq
curl 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108' | jq

# Get a value set by id (pick the first one)
id=`curl 'http://localhost:8080/fhir/r4/ValueSet' | jq -r '.entry[0].resource.id'`
curl "http://localhost:8080/fhir/r4/ValueSet/$id" | jq

# Perform an $expand operation on the implicit ValueSet representing SNOMEDCT
curl 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs' | jq

# Perform an $expand operation on an explicit value set
curl 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108' | jq

# Perform a SNOMEDCT search via a ValueSet $expand with a filter parameter
curl 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes' | jq
curl 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes' | jq

# Perform a SNOMEDCT search via a ValueSet $expand with a filter and an ECL expression
curl 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/%3C%3C128927009&filter=gastrointestinal' | jq
```

### Testing the FHIR R5 API

The following code block has a number of curl commands that test a few of the FHIR R5 API endpoints of the server to demonstrate basic function.

```
# Find CodeSystems
curl 'http://localhost:8080/fhir/r5/CodeSystem' | jq

# Perform a SNOMEDCT CodeSystem $lookup for a code
curl 'http://localhost:8080/fhir/r5/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009' | jq

# Find ConceptMaps
curl 'http://localhost:8080/fhir/r5/ConceptMap' | jq

# Perform a ConceptMap $translate to find "target" codes for a SNOMEDCT code
curl 'http://localhost:8080/fhir/r5/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106&sourceSystem=http://snomed.info/sct&sourceCode=300862005' | jq

# Find implied ValueSets for CodeSystems and explicit value sets
curl 'http://localhost:8080/fhir/r5/ValueSet' | jq
curl 'http://localhost:8080/fhir/r5/ValueSet?url=http://snomed.info/sct?fhir_vs' | jq
curl 'http://localhost:8080/fhir/r5/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108' | jq

# Get a value set by id (pick the first one)
id=`curl 'http://localhost:8080/fhir/r5/ValueSet' | jq -r '.entry[0].resource.id'`
curl "http://localhost:8080/fhir/r5/ValueSet/$id" | jq

# Perform an $expand operation on the implicit ValueSet representing SNOMEDCT
curl 'http://localhost:8080/fhir/r5/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs' | jq

# Perform an $expand operation on an explicit value set
curl 'http://localhost:8080/fhir/r5/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108' | jq

# Perform a SNOMEDCT search via a ValueSet $expand with a filter parameter
curl 'http://localhost:8080/fhir/r5/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes' | jq
curl 'http://localhost:8080/fhir/r5/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes' | jq

# Perform a SNOMEDCT search via a ValueSet $expand with a filter and an ECL expression
curl 'http://localhost:8080/fhir/r5/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/%3C%3C128927009&filter=gastrointestinal' | jq
```

**[Back to top](#step-by-step-instructions-with-sandbox-data)**

