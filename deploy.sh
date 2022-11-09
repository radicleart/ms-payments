#!/bin/bash -e
#
############################################################

export DEPLOYMENT=$1
export SERVER=popper.brightblock.org
export DOCKER_ID_USER='mijoco'
export DOCKER_CMD='docker'
export DOCKER_COMPOSE_CMD='docker-compose'
export PORT=22
export SERVER=popper.brightblock.org
if [ "$DEPLOYMENT" == "prod" ]; then
  SERVER=chomsky.brightblock.org;
  PORT=7019
fi

echo --- ms_payments:copying to [ $PATH_DEPLOY ] --------------------------------------------------------------------------------;
printf "\n\n Connectiong to $SERVER.\n"

ssh -i ~/.ssh/id_rsa -p $PORT bob@$SERVER "
  cd /home/bob/hubgit/ms_payments
  # git pull
  # cp .env.production .env
  cat .env
  docker login
  . ~/.profile
  $DOCKER_COMPOSE_CMD -f docker-compose-images.yml pull
  $DOCKER_COMPOSE_CMD -f docker-compose-images.yml down
  $DOCKER_COMPOSE_CMD -f docker-compose-images.yml up -d
";

#ssh -i ~/.ssh/id_rsa -p $PORT bob@$SERVER "
#  cd /home/bob/hubgit/stxeco-server
#  cat .env
#  docker login
#  . ~/.profile
#  $DOCKER_CMD  pull  
#  $DOCKER_CMD  stop $SERVICE
#  $DOCKER_CMD  rm $SERVICE 
#  $DOCKER_CMD  create $SERVICE
#  $DOCKER_CMD  start $SERVICE
#";

printf "Finished....\n"
printf "\n-----------------------------------------------------------------------------------------------------\n";

exit 0;
