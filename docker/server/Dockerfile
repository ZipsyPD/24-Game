FROM eclipse-temurin:8-jdk

WORKDIR /app

COPY src /app/src
COPY lib /app/lib
COPY policy.policy /app/policy.policy

COPY docker/glassfish/glassfish5/glassfish /opt/glassfish

RUN mkdir -p /app/build && \
    javac -d /app/build \
      -cp "/opt/glassfish/lib/gf-client.jar:/opt/glassfish/modules/javax.jms-api.jar:/app/lib/mysql-connector-j.jar" \
      /app/src/main/java/*.java

EXPOSE 1099 1100

CMD ["sh", "-c", "cd /app/build && rmiregistry 1099 & sleep 2 && cd /app && java -Djava.rmi.server.hostname=localhost -Djava.security.policy=/app/policy.policy -cp '/app/build:/opt/glassfish/lib/gf-client.jar:/opt/glassfish/modules/javax.jms-api.jar:/app/lib/mysql-connector-j.jar' Server"]
