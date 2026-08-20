FROM eclipse-temurin:8-jdk

WORKDIR /opt

COPY glassfish5 /opt/glassfish5

EXPOSE 8080 4848 3700 7676

CMD ["/opt/glassfish5/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]

