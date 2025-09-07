# Step-by-step instructions with TermHub data
Instructions on using data from a TermHub project to Open Termhub up and running within 5 minutes.

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
export JAVA_OPTS=-Xmx16g
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR
make build run
```

### Option 2: run with public docker image

The final option is to run the latest published public docker image as a container with an INDEX_DIR environment variable to specify where the Lucene indexes should live (make sure this directory exists):

```
# On Windows use export INDEX_DIR=c:/tmp/opentermhub/index
export INDEX_DIR=/tmp/opentermhub/index
/bin/rm -rf $INDEX_DIR/*; mkdir -p $INDEX_DIR; chmod -R a+rwx $INDEX_DIR
docker run -d --rm --name open-termhub \
  -e ENABLE_POST_LOAD_COMPUTATIONS=true -e JAVA_OPTS="-Xmx16g" \
  -v "$INDEX_DIR":/index -p 8080:8080 wcinformatics/open-termhub:latest
```

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## View API Documentation

All three of the above options will yield a running server and you should now you should be able to access the Swagger UI pages:
* Swagger [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* FHIR R4 Swagger [http://localhost:8080/fhir/r4/swagger-ui/index.html](http://localhost:8080/fhir/r4/swagger-ui/index.html)
* FHIR R5 Swagger [http://localhost:8080/fhir/r5/swagger-ui/index.html](http://localhost:8080/fhir/r5/swagger-ui/index.html)


**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Create TermHub account/login

### Creating an account (skip if you have one)

Start by going to the [Termhub Signup Page](https://app.terminologyhub.com/signup).  You can sign up with a username/password or via social login using a Google or Microsoft account.  Your email address will be your username.

### Logging in (once you have created an account)

Go to the [Termhub Login Page](https://app.terminologyhub.com/login) and log in using the account created in the previous step.

**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Creating a TermHub Project

To properly test this, you'll want to create a TermHub project with the terminologies that you want to load into the Open TermHub container.

### Steps after logging into TermHub

* **Click the "Projects" sidebar item**

<img width="200px" src="images/choose-sidebar-projects.png">

* **Click the "New Project" button**

<img width="100px" src="images/new-project-button.png">

* **Select your organization**
* **Set "Project Name" to "OpenTermhub Test Project"**
* **Set "Project Description" to "OpenTermhub Test Project"**

<img width="600px" src="images/configure-new-project.png">


* **Scroll down to choose terminologies to add**
  * **SNOMEDCT_US latest (also select the ICD10CM maps and the extension subset)**
  
<img width="800px" src="images/configure-snomedct_us.png">
  
  * **LOINC latest**

<img width="800px" src="images/configure-loinc.png">

  * **ISO-639-1 latest**
  * **ISO-639-2 latest**
  
<img width="800px" src="images/configure-iso.png">
  
* **Scroll to the bottom and click "Create project"**

<img width="800px" src="images/create-project.png">

* **On the project details screen, choose the icon to download all** (choose "Format: FHIR R5 json)

<img width="800px" src="images/download-all.png">

At this point, you will have downloaded .zip files of all the terminologies set up in the project above.

<img width="800px" src="images/downloads-directory.png">

The next step is to unpack all of these .zip files and put the resulting .json files all together in the same directory, so that we can run the commands to load this data into the Open TermHub server that was launched at the top.


**[Back to top](#step-by-step-instructions-with-termhub-data)**

## Loading data

After the previous step, you will have CodeSystem .json files downloaded from TermHub and these can now be loaded through the API into a running container.  The following assumes that all of 
the zip files are unpacked and resulting .json files in a local directory

#### Load SNOMEDCT_US

```
f=CodeSystem*snomedct_us*json
curl -X POST http://localhost:8080/fhir/r5/CodeSystem \
  -H 'accept: application/fhir+json' -H 'Content-Type: application/fhir+json' \
  -d "@CodeSystem-snomedct_us-nlm-20250301-r5.json" | jq
```




**[Back to top](#step-by-step-instructions-with-termhub-data)**


## Demonstrating the UI

try these curl commands
OR try the postman collecition (postman-tutorial.json)

**[Back to top](#step-by-step-instructions-with-termhub-data)**
