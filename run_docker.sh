#!/usr/bin/env bash

if [ $# -ne 2 ]; then
  echo '[ERROR] please add your com.doubleknd26.macro type and condig file password as args e.g) ./run_docker.sh {macro_type} {gpg_password}'
  exit 1 
fi

macro_type=$1
config_file_password=$2
echo '[INFO] com.doubleknd26.macro type:' $macro_type
echo '[INFO] config file password:' $config_file_password 

#TODO: check com.doubleknd26.macro type format

# build docker
./gradlew clean :dockerBuild -Dtype=$macro_type -Dpw=$config_file_password

# run docker
docker run -d -p 4444:4444 -p 11619:11619 --shm-size=128m -v /tmp/logs:/tmp/logs --name macro_app com.doubleknd26.macro

