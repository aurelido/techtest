FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    PAYMENTS_SLEEP=0 \
    JAVA_OPTS=""

# Add a payments user to run our application so that it doesn't need to run as root
RUN adduser -D -s /bin/sh payments
WORKDIR /home/payments

ADD entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown payments:payments entrypoint.sh
USER payments

ADD payments.jar payments.jar

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8081 5701/udp

