FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

USER root
RUN mkdir -p /usr/local/newrelic && \
    curl -L -o /tmp/newrelic-java.zip https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip /tmp/newrelic-java.zip -d /usr/local/newrelic/ && \
    mv /usr/local/newrelic/newrelic-agent-*/newrelic.jar /usr/local/newrelic/ && \
    rm -rf /tmp/newrelic-java.zip /usr/local/newrelic/newrelic-agent-* && \
    chown -R 185:185 /usr/local/newrelic
ADD newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

# Ensure proper permissions on the yml file too
RUN chown 185:185 /usr/local/newrelic/newrelic.yml

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-javaagent:/usr/local/newrelic/newrelic.jar -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]