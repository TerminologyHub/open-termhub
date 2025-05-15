# Step-by-step instructions with Sandbox data
Instructions on using data local to this project to get Open Termhub up and running within 5 minutes.

[Tutorial Training Video](https://youtu.be/Vto42DIMw2U)

## Prerequisites/Setup
* Java 17+
* This tutorial assumes you have cloned this open-termhub repository.

## Build and run the server

### Option 1: build/run without docker

One option is to just build the code and run the server locally and use an INDEX_DIR environment variable to specify the directory where Lucene indexes should live (make sure this directory exists)

```
export INDEX_DIR=/tmp/opentermhub/index
mkdir -p $INDEX_DIR
make build
make run
```

### Option 2: build/run with docker

the other option is to build the docker image and run as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists)

```
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
make docker
image=wcinformatics/open-termhub:`grep "version = " build.gradle | cut -d\' -f 2`
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" -v "$INDEX_DIR":/index -p 8080:8080 $image

docker run -d --rm --name open-termhub -p 8080:8080 $image
```

### Option 3: run with public docker image

The final option is to run the latest published public docker image as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists):

```
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" -v "$INDEX_DIR":/index -p 8080:8080 termhub/open-termhub:latest
```

**[Back to top](#step-by-step-instructions-with-sandbox-data)**

## View API Documentation

All three of the above options will yield a running server and you should now you should be able to access the Swagger UI pages:
* Swagger [http://localhost:8080/swagger-ui/index.html](https://localhost:8080/swagger-ui/index.html)
* FHIR R4 Swagger [http://localhost:8080/fhir/r4/swagger-ui/index.html](https://localhost:8080/fhir/r4/swagger-ui/index.html)
* FHIR R5 Swagger [http://localhost:8080/fhir/r5/swagger-ui/index.html](https://localhost:8080/fhir/r5/swagger-ui/index.html)

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

#### Load Sandbox SNOMEDCT

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-snomedct-sandbox-20240101-r5.json'
```

#### Load Sandbox SNOMEDCT_US

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-snomedctus-sandbox-20240301-r5.json'
```

#### Load Sandbox RXNORM

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-rxnorm-sandbox-04012024-r5.json'
```

#### Load Sandbox ICD10CM

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-icd10cm-sandbox-2023-r5.json'
```

#### Load Sandbox LNC

```
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/CodeSystem-lnc-sandbox-277-r5.json'
```

#### Load the SNOMEDCT_US to ICD10CM concept maps

```
curl -X POST http://localhost:8080/fhir/r5/ConceptMap \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d '@src/main/resources/data/ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json'
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
# Find terminologies
curl http://localhost:8080/terminology | jq

# Get a SNOMEDCT concept by code
curl http://localhost:8080/concept/SNOMEDCT/107907001 | jq

# Perform a SNOMEDCT search with a query
curl http://localhost:8080/concept?terminology=SNOMEDCT&query=cancer&include=minimal | jq

# Perform a SNOMEDCT search with a query and an ECL expression
curl "http://localhost:8080/concept?terminology=SNOMEDCT&query=cancer&expression=<<128927009&include=minimal" | jq

# Find mapsets

# Find "target" codes of mappings for a particular SNOMEDCT code
```

### Testing the FHIR R4 API

The following code block has a number of curl commands that test a few of the FHIR R4 API endpoints of the server to demonstrate basic function.

```
# Find CodeSystems
curl -X POST http://localhost:8080/fhir/r4/CodeSystem | jq

# Perform a SNOMEDCT CodeSystem $lookup for a code

# Find implied ValueSets for CodeSystems

# Perform a SNOMEDCT search via a ValueSet $expand with a filter

# Perform a SNOMEDCT search via a ValueSet $expand with a filter and an ECL expression

# Find ConceptMaps

# Perform a ConceptMap $translate to find "target" codes for a SNOMEDCT code
```

### Testing the FHIR R5 API

The following code block has a number of curl commands that test a few of the FHIR R5 API endpoints of the server to demonstrate basic function.

```
# Find CodeSystems
curl -X POST http://localhost:8080/fhir/r5/CodeSystem | jq

# Perform a SNOMEDCT CodeSystem $lookup for a code

# Find implied ValueSets for CodeSystems

# Perform a SNOMEDCT search via a ValueSet $expand with a filter

# Perform a SNOMEDCT search via a ValueSet $expand with a filter and an ECL expression

# Find ConceptMaps

# Perform a ConceptMap $translate to find "target" codes for a SNOMEDCT code
```


**[Back to top](#step-by-step-instructions-with-sandbox-data)**

