#!/usr/bin/env bash

DOCKER=/usr/bin/docker
if [ -z ${host_postgres_port+x} ]; then
  host_postgres_port=15432
fi
container_postgres_port=5432

function start_postgresql_in_container() {
  time $DOCKER pull sameersbn/postgresql:9.4
  time $DOCKER run --name postgresql -d -p $host_postgres_port:$container_postgres_port -e 'DB_USER=va' -e 'DB_PASS=va' -e 'DB_NAME="va-dev"' sameersbn/postgresql:9.4
}

function wait_for_postgresql_to_be_available() {
  echo "Waiting for Postgresql to be listening to its port..."
  attempt=0
  max_attempts=120
  interval_seconds=0.5
  until (nc -z `$DOCKER inspect --format='{{.NetworkSettings.IPAddress}}' postgresql` $container_postgres_port ) || [[ $attempt -ge $max_attempts ]] ; do
    echo "  Waiting for Postgresql to be available in the container , attempt $attempt/$max_attempts ..."
    sleep $interval_seconds
    attempt=$(( $attempt + 1 ))
  done
  if [ $attempt -eq $max_attempts ]; then
    echo "Could not find running Postgresql in $max_attempts attempts with $interval_seconds second intervals, failing."
    remove_postgresql_container
    exit 2
  fi
}

function give_public_schema_to_va() {
  echo 'echo "alter schema public owner to va;" | sudo -u postgres psql -d va-dev -f -' | $DOCKER exec -i postgresql /bin/bash -c 'cat > /tmp/give_public_schema_to_va.bash'
  $DOCKER exec postgresql bash -e /tmp/give_public_schema_to_va.bash
}

function remove_postgresql_container() {
  $DOCKER stop postgresql
  $DOCKER rm -v postgresql
}