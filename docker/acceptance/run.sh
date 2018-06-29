#!/bin/sh
START_TIME=$SECONDS
#set environment variables
export DOCKER_HOST_IP=$(docker-machine ip $(docker-machine active))
export APP_IP=$DOCKER_HOST_IP
export APP_PORT="8080"
export CI_STAGE="ACCEPTANCE"

#Remove existing containers
docker-compose down

#start Cassandra first
docker-compose up -d cassandra

while ! nc -z $DOCKER_HOST_IP 9042; do
  echo "Waiting for Cassandra to start..."
  sleep 2;
done

#start containers
docker-compose up -d

while ! nc -z $DOCKER_HOST_IP $APP_PORT; do
  echo "Waiting for Application to start..."
  sleep 2;
done

#run tests
cd ../..
mvn verify

#tear down
cd -
docker-compose down -v
echo "Total time: $(($SECONDS - $START_TIME))"