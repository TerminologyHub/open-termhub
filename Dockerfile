#######################################################################
#                              Base Node                              #
#######################################################################
#
# This is the build container
#
FROM gradle:8.11.0-jdk17 AS gradle-build
USER root
RUN apt-get install -y bash procps

# Set environment
ENV HOME=/home/gradle
ENV PROJECT=/home/gradle/project
ENV GRADLE_USER_HOME="${HOME}/.gradle"

# Setup gradle user
RUN mkdir -p "${PROJECT}"
RUN chown -R gradle:gradle "${PROJECT}"
USER gradle
RUN mkdir -p "${PROJECT}/build/output"
RUN mkdir -p "${PROJECT}/.gradpwdle"
RUN mkdir -p "${PROJECT}/.cache"

# copy source
COPY --chown=gradle:gradle config/ "${PROJECT}"/config
COPY --chown=gradle:gradle src/ "${PROJECT}/src"
COPY --chown=gradle:gradle [bs]*.gradle ${PROJECT}/
RUN chown -R gradle:gradle "${PROJECT}"

WORKDIR ${PROJECT}

# Skip tests for docker build
RUN gradle bootJar -x test -x spotbugsMain -x spotbugsTest

#
# This is the runtime container
#
FROM bellsoft/liberica-openjre-alpine:17.0.13

# Create the user that will be used to run the product and set up the directory it'll reside in.
ARG RUNTIME_USER=server
RUN addgroup -S -g 1001 ${RUNTIME_USER}
RUN mkdir /srv/rt
RUN adduser -D -S -H -G ${RUNTIME_USER} -u 1001 -s /bin/false -h /srv/rt ${RUNTIME_USER}
RUN chown -R ${RUNTIME_USER}:${RUNTIME_USER} /srv/rt

# Copy the launcher script in.
# This serves to ensure that all necessary dependencies end up on the classpath.
ADD run.sh /srv/rt/
RUN chmod +x /srv/rt/run.sh

# Switch to the runtime user and copy the product in.
USER $RUNTIME_USER
COPY --from=gradle-build --chown=server:server /home/gradle/project/build/libs/ /srv/rt/
WORKDIR /srv/rt

# Set runtime environment variables
ENV ENABLE_POST_LOAD_COMPUTATIONS=false

# Run the built product when the container launches
CMD ["/srv/rt/run.sh"]