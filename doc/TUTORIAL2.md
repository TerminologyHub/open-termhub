# Step-by-step instructions with TermHub data
Instructions on using data from a TermHub project to Open Termhub up and running within 5 minutes.

[Tutorial Training Video](https://youtu.be/Vto42DIMw2U)

## Prerequisites/Setup
* Java 17+
* This tutorial assumes you have cloned this open-termhub repository.

## Build and run the server

### Option 1: build/run without docker

One option is to just build the code and run the server locally and use an INDEX_DIR environment variable to specify the directory where Lucene indexes should live (make sure this directory exists)

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR
make build
make run
```

### Option 2: build/run with docker

the other option is to build the docker image and run as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists)

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
make docker
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" \
  -v "$INDEX_DIR":/index -p 8080:8080 wcinformatics/open-termhub:latest
```

### Option 3: run with public docker image

The final option is to run the latest published public docker image as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists):

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
docker run -d --rm --name open-termhub -e INDEX_DIR="/index" \
  -v "$INDEX_DIR":/index -p 8080:8080 wcinformatics/open-termhub:latest
```

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## View API Documentation

All three of the above options will yield a running server and you should now you should be able to access the Swagger UI pages:
* [Swagger](https://localhost:8080/swagger-ui/index.html)
* [FHIR R4 Swagger](https://localhost:8080/fhir/r4/swagger-ui/index.html)

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Create TermHub account/login

### Creating an account (skip if you have one)

Start by going to the [Termhub Signup Page](https://app.terminologyhub.com/signup).  You can sign up with a username/password or via social login using a Google or Microsoft account.  Your email address will be your username.

### Logging in (once you have created an account)

Go to the [Termhub Login Page](https://app.terminologyhub.com/login) and log in using the account created in the previous step.

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Creating a TermHub Project

To properly test this, you'll want to create a TermHub project with the terminologies that you want to load into the Open TermHub container.

Steps
* Go to "Projects" sidebar
* Click "New Project"
* Set "Project Name" to "OpenTermhub Test Project"
* Set "Project Description" to "OpenTermhub Test Project"
* Scroll down to choose terminologies to add
  * SNOMEDCT
  * LOINC
  * ISO-639-1
  * ISO-639-2
* Click "Create project" - you can always get back to this project from your "dashboard" or from the project list on the "Projects" sidebar item.
* Click the icon to download all terminologies in your project
<need image>

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Loading data

After the previous step, you will have CodeSystem .json files downloaded from TermHub and these can now be loaded through the API into a running container.

... curl commands ... info about runtime

**[Back to top](#step-by-step-instructions-with-termhub-data)**


## Demonstrating the UI

try these curl commands
OR try the postman collecition (postman-tutorial.json)

**[Back to top](#step-by-step-instructions-with-termhub-data)**
