# Step-by-step instructions with sandbox data
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
mkdir -p $INDEX_DIR
make docker
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" -v "$INDEX_DIR":/index -p 8080:8080 termhub/open-termhub:1.1.0-SNAPSHOT
```

### Option 3: run with public docker image

The final option is to run the latest published public docker image as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists):

```
export INDEX_DIR=/tmp/opentermhub/index
mkdir -p $INDEX_DIR
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" -v "$INDEX_DIR":/index -p 8080:8080 termhub/open-termhub:1.1.0.202505
```

## View API Documentation

All three of the above options will yield a running server and you should now you should be able to access the Swagger UI pages:
* [Swagger](https://localhost:8080/swagger-ui/index.html)
* [FHIR R4 Swagger](https://localhost:8080/fhir/r4/swagger-ui/index.html)
* [FHIR R5 Swagger](https://localhost:8080/fhir/r5/swagger-ui/index.html)

## Loading SANDBOX data

... load via API from src/main/resources/data

curl -X 'POST' \
  'http://localhost:8080/fhir/r4/CodeSystem' \
  -H 'accept: application/fhir+json' \
  -H 'Content-Type: application/fhir+json' \
  -d '{ Content from code system file }'
  
curl -X 'POST' \
  'http://localhost:8080/fhir/r5/CodeSystem' \
  -H 'accept: application/fhir+json' \
  -H 'Content-Type: application/fhir+json' \
  -d '{ Content from code system file }'
  
curl -X 'POST' \
  'http://localhost:8080/fhir/r4/ConceptMap' \
  -H 'accept: application/fhir+json' \
  -H 'Content-Type: application/fhir+json' \
  -d '{ Content from concept map file as json }'
  
curl -X 'POST' \
  'http://localhost:8080/fhir/r5/ConceptMap' \
  -H 'accept: application/fhir+json' \
  -H 'Content-Type: application/fhir+json' \
  -d '{ Content from concept map file as json }'

## Demonstrating the UI

try these curl commands
OR try the postman collecition (postman-tutorial.json)
