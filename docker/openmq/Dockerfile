FROM eclipse-temurin:8-jdk

WORKDIR /opt

COPY mq /opt/mq

ENV IMQ_HOME=/opt/mq
ENV IMQ_VARHOME=/var/openmq

RUN mkdir -p /var/openmq

EXPOSE 7676 7677

CMD ["/opt/mq/bin/imqbrokerd", "-Dimq.jms.tcp.port=7677", "-tty"]
