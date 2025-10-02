# Deploying Open TermHub with Full Terminologies from TermHub

This documentation describes considerations and steps for creating a TermHub project,
configuring a number of terminologies such as SNOMEDCT_US, RXNORM, ICD10CM, and LOINC
and deploying them to a running container.

This includes considerations about using syndication (or not) as well as memory usage
and disk space requirements for a variety of situations.  Also included is a discussion
of the use of the additional features such as enabling post load calculations and use of
the embedded terminology browser.

## Configuration Options

### Environment Variables

The following environment variables can be used to configure the application:

- `DEBUG`: set to "true" to see debug messages in the log
- `JAVA_OPTS`: Java options including memory usage, recommendation is to use `-Xmx4g` but it can load full data with `-Xmx2g`
- `ENABLE_POST_LOAD_COMPUTATIONS`: Enable/disable post-load computations (default: false).  This can be set to "true" to compute tree position information which is used by the embedded browser to display hierarchy information.
- `PROJECT_API_KEY`: Authentication token for secure operations (required only if using Terminology Syndication from www.terminologyhub.com). To obtain this token:
  1. Visit [www.terminologyhub.com](https://www.terminologyhub.com)
  2. Navigate to your project's edit page
  3. Either generate a new token or copy the existing "Project API Key"

## Data Persistence

The application uses Lucene for fast searching of terminology data. By default, indexes are stored in `/index`. To persist these indexes between container restarts, mount a volume to `/index` on the container.  For example, 

```bash
docker run --rm --name open-termhub -p 8080:8080 \
  -v /path/to/index/folder:/index \
  wcinformatics/open-termhub:latest
```

### Volume size

The exact size needed for the volume varies and depends on the number and size of terminology assets included in the deployment.  For example, a configuration that loads the latest versions of SNOMEDCT_US, RXNORM, LOINC, and ICD10CM with `ENABLE_POST_LOAD_COMPUTATIONS=true` a volume size of 20GB is recommended.  The same data set up with `ENABLE_POST_LOAD_COMPUTATIONS=false` requires a small volume size of 10GB.  

The best strategy is to load the data assets you need in an "offline" environment and you can see exactly how much space is needed, then provision a volume for the container that is large enough for that size.

## Creating a TermHub Project with an API Key

The primary content provider for Open TermHub is the TermHub (cloud) application itself.  The idea is that you can use TermHub to configure "projects" that have terminology specifications that you want to use.

Start by logging into your account

## Loading the Container

There are a few options for loading a container including: 
* Pre-loading a volume offline and then mounting it to a container
* Using syndication to have a container load itself upon startup
* Launching a container and loading it from "downloaded" artifacts


### Pre-loading a Volume

This option is recommended for situations where you want to control terminology versions and ensure the data being
served by the container is exactly what is needed by downstream services.  This is also a recommended option for
running with Kubernetes because it will allow you to scale pods in "read only" mode that are mounted with the same
persistent volume.   

This option is NOT recommended if you want to use "latest" versions and have the container automatically "update"
content. This option is NOT recommended if you need to make write calls to the container and intend to have multiple
pods running.

Pre-loading a volume can use either of the "syndication" or "direct load" strategies discussed below.  The difference
is that you run this process on your own separately from the deployment and then shut down the container, package
up the index directory data and prepare it to be mounted as a volume to containers to be deployed either directly
(via something like EC2 or Fargate) or as part of an orchestration platform (fia something like Kubernetes)

### Loading Data with Syndication

Open TermHub supports syndication of content from a TermHub project that is configured with an API key and one or
more terminology/subset/mapset (code system, value set, concept map) resources.  [See the section above](#creating-a-termhub-project-with-an-api-key) for information on setting up a project.

If the container is launched with a `PROJECT_API_KEY` environment variable set to the project API key of a corresponding TermHub project, then the container will load itself upon startup.  This mode can be used either to pre-load some data to be packaged and deployed to a container service later -- or it can be used simply to streamline loading data into a running container.

If the TermHub project has been configured with "latest" versions of terminologies, it is possible to configure "re-syndication" of the container to grab new versions of the content when available.  In this mode, when new versions are available the container "reconciles" the feed defined by the project with data that is loaded - so it is possible to tweak
the contents of the deployed service simply by changing the configuration in TermHub (cloud). This "resyndication" option is configured by a `SYNDICATION_CHECK_CRON` environment variable, which uses cron-style configuration to specify when the container should attempt resyndication. 

* e.g. `0 0 * * *` for daily check at midnight

If using resyndication, it is NOT recommended to run multiple containers mounted to the same indexes volume that are both configured for resyndication because both deployments will attempt to load data on the same schedule, causing a conflict.


### Loading Data Manually




