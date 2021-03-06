#!/usr/bin/env bash

if [ $# -ne 1 ]; then
  echo '[ERROR] please add your macro type and condig file password as args e.g) ./run_docker.sh {type}'
  exit 1
fi

type=$1

# read it from the local env to prevent exposure 
slack_webhook_url=`echo $SLACK_WEBHOOK_URL`
echo '[INFO] type:' $type
echo '[INFO] slack_webhook_url:' $slack_webhook_url

# build docker
./gradlew clean :dockerBuild -Dtype=$type -Dslack_webhook_url=$slack_webhook_url

# run docker
sudo docker run -d -p 4444:4444 -p 11619:11619 --shm-size=128m -v /tmp/logs:/tmp/logs --name macro_app macro 

