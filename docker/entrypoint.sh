#!/bin/sh

echo "The application will start in ${PAYMENTS_SLEEP}s..." && sleep ${PAYMENTS_SLEEP}
exec java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/app.war" "$@"
