#!/bin/bash -e
#
############################################################

export DEPLOYMENT=$1
export SERVER=popper.brightblock.org
export DOCKER_ID_USER='mijoco'
export DOCKER_CMD='docker'
export PORT=22
export SERVER=popper.brightblock.org
if [ "$DEPLOYMENT" == "prod" ]; then
  SERVER=chomsky.brightblock.org;
  PORT=7019
fi

mvn -f ./pom.xml -Dmaven.test.skip=true clean install

$DOCKER_CMD build . -t mijoco/ms_payments
$DOCKER_CMD tag mijoco/ms_payments  mijoco/ms_payments
$DOCKER_CMD push mijoco/ms_payments:latest

echo --- ms_payments:copying to [ $PATH_DEPLOY ] --------------------------------------------------------------------------------;
printf "\n\n Connectiong to $SERVER.\n"

exit 0;
