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

- `ENABLE_POST_LOAD_COMPUTATIONS`: Enable/disable post-load computations (default: false)
- `TERMHUB_TOKEN`: Authentication token for secure operations (required only if using Terminology Syndication from www.terminologyhub.com). To obtain this token:
  1. Visit [www.terminologyhub.com](https://www.terminologyhub.com)
  2. Navigate to your project's edit page
  3. Either generate a new token or copy the existing "Project API Key"

## Data Persistence

The application uses Lucene for fast searching of terminology data. By default, indexes are stored in `/tmp/opentermhub/index`. To persist these indexes between container restarts:

```bash
docker run --rm --name open-termhub -p 8080:8080 \
  -v /path/to/your/data:/tmp/opentermhub/index \
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
      - ENABLE_POST_LOAD_COMPUTATIONS=true # Optional: default is false
      - TERMHUB_TOKEN=${TERMHUB_TOKEN}     # Optional: only required if using Terminology Syndication from www.terminologyhub.com
    volumes:
      - ./data:/tmp/opentermhub/index      # Optional: index store location
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
