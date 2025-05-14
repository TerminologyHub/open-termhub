# Step-by-step instructions with sandbox data
Instructions on using data local to this project to get Open Termhub up and running within 5 minutes. [Click here to see general build documentation](BUILD.md).

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

```



### Option 2: build/run with docker
