#!/bin/bash

if [ "$#" != 7 ]; then
 echo " Usage: ./run-demo.sh access_key secret_key video_src user pass stream_name region "
 exit
fi
ACCESS_KEY=$1
SECRET_KEY=$2
VIDEO_SRC=$3
USER=$4
PASS=$5
STREAM_NAME=$6
REGION=$7

# Create a temporary filename in /tmp directory
#jar_files=$(mktemp)
# Create classpath string of dependencies from the local repository to a file
#mvn -Dmdep.outputFile=$jar_files dependency:build-classpath
#export LD_LIBRARY_PATH=/opt/amazon-kinesis-video-streams-producer-sdk-cpp/kinesis-video-native-build/downloads/local/lib:$LD_LIBRARY_PATH
#classpath_values=$(cat $jar_files)
# Start the demo app
java -classpath target/kvsdemo-1.0-SNAPSHOT.jar:$classpath_values -Daws.accessKeyId=${ACCESS_KEY} -Daws.secretKey=${SECRET_KEY} -Djava.library.path=/opt/amazon-kinesis-video-streams-producer-sdk-cpp/kinesis-video-native-build brainstech.kvsdemo.OpenCVDemo ${VIDEO_SRC} ${USER} ${PASS} ${STREAM_NAME} ${REGION}

