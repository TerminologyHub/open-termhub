# Docker Instructions for Open TermHub

## Quick Start

The fastest way to get started is using our official Docker image:

```bash
docker run --rm --name open-termhub -p 8080:8080 wcinformatics/open-termhub:latest
```

This will start the server with default settings. The application will be available at `http://localhost:8080`. The `--rm` flag automatically removes the container when it stops.

## Configuration Options

### Environment Variables

The following environment variables can be used to configure the application:

- `DEBUG`: set to "true" to see debug messages in the log
- `JAVA_OPTS`: Java options including memory usage, recommendation is to use `-Xmx4g` but it can load full data with `-Xmx2g`
- `ENABLE_POST_LOAD_COMPUTATIONS`: Enable/disable post-load computations (default: false)
- `PROJECT_API_KEY`: Authentication token for secure operations (required only if using Terminology Syndication from www.terminologyhub.com). To obtain this token:
  1. Visit [www.terminologyhub.com](https://www.terminologyhub.com)
  2. Navigate to your project's edit page
  3. Either generate a new token or copy the existing "Project API Key"
- `SYNDICATION_CHECK_ON_STARTUP` (optional): when set to `true`, performs a one-time syndication load at application startup. If not set or false, no startup syndication occurs.
- `SYNDICATION_CHECK_CRON` (optional): a Spring cron expression (6 fields: sec min hour dom mon dow) to enable periodic re-syndication. If not set or empty, no schedule is registered. Requires `PROJECT_API_KEY`.

## Data Persistence

The application uses Lucene for fast searching of terminology data. By default, indexes are stored in `/index`. To persist these indexes between container restarts:

```bash
docker run --rm --name open-termhub -p 8080:8080 \
  -v /path/to/index/folder:/index \
  wcinformatics/open-termhub:latest
```

## Docker Compose Example

For production deployments:

```yaml
version: '3.8'
services:
  termhub:    # This is the container name in Docker Compose
    image: wcinformatics/open-termhub:latest
    ports:
      - "8080:8080"
    environment:
      - ENABLE_POST_LOAD_COMPUTATIONS=true     # Optional: default is false
      - PROJECT_API_KEY=${PROJECT_API_KEY}     # Optional: only required if using Terminology Syndication from www.terminologyhub.com
      - SYNDICATION_CHECK_ON_STARTUP=${SYNDICATION_CHECK_ON_STARTUP} # Optional
      - SYNDICATION_CHECK_CRON=${SYNDICATION_CHECK_CRON}             # Optional, Spring cron format
    volumes:
      - ./data:/index      # Optional: index store location
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

## Syndication usage

Syndication requires `PROJECT_API_KEY`. Configure one or both of the following:

- One-time startup load:

```
docker run --rm --name open-termhub -p 8080:8080 \
  -e PROJECT_API_KEY=... \
  -e SYNDICATION_CHECK_ON_STARTUP=true \
  wcinformatics/open-termhub:latest
```

- Periodic re-syndication (Cron format):

```
docker run --rm --name open-termhub -p 8080:8080 \
  -e PROJECT_API_KEY=... \
  -e SYNDICATION_CHECK_CRON="0 0 0 * * *" \
  wcinformatics/open-termhub:latest
```

- Both together:

```
docker run --rm --name open-termhub -p 8080:8080 \
  -e PROJECT_API_KEY=... \
  -e SYNDICATION_CHECK_ON_STARTUP=true \
  -e SYNDICATION_CHECK_CRON="0 0 0 * * *" \
  wcinformatics/open-termhub:latest
```

## Building from Source

To build the Docker image from source:

```bash
git clone https://github.com/wcinformatics/open-termhub.git
cd open-termhub
make docker
```
