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

The exact size needed for the volume varies and depends on the number and size of terminology assets included in 
the deployment.  For example, a configuration that loads the latest versions of SNOMEDCT_US, RXNORM, LOINC, and
ICD10CM with `ENABLE_POST_LOAD_COMPUTATIONS=true` a volume size of XxGB is recommended.  The same data set up with
`ENABLE_POST_LOAD_COMPUTATIONS=false` requires a small volume size of XxGB 

And here is an example of using docker compose:

```yaml
version: '3.8'
services:
  termhub:    # This is the container name in Docker Compose
    image: wcinformatics/open-termhub:latest
    ports:
      - "8080:8080"
    environment:
      - ENABLE_POST_LOAD_COMPUTATIONS=true # Optional: default is false
      - PROJECT_API_KEY=${PROJECT_API_KEY} # Optional: only required if using Terminology Syndication from www.terminologyhub.com
      - JAVA_OPTS=-Xmx4g                   # Recommended
    volumes:
      - ./data:/index                      # Optional: index store location for persistent retrieval
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```


## Health Checks

The application provides health endpoints:
- Health check: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`

## Troubleshooting

1. **Container fails to start**:
   - Check logs: `docker logs termhub`
   - Verify environment variables are set correctly
   - Ensure required volumes are mounted properly

2. **Performance issues**:
   - Consider adjusting Java heap size: `-e JAVA_OPTS="-Xmx8g"`
   - Monitor container resources: `docker stats termhub`

3. **Network issues**:
   - Verify port mappings
   - Check network connectivity: `docker network inspect bridge`

## Building from Source

To build the Docker image from source:

```bash
git clone https://github.com/wcinformatics/open-termhub.git
cd open-termhub
make docker
```
