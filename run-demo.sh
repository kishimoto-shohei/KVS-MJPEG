#!/bin/bash

if [ "$#" != 6 ]; then
 #echo " Usage: ./run-demo.sh access_key secret_key video_src user pass stream_name region "
 echo " Usage: ./run-demo.sh video_src user pass stream_name region fps"
 exit
fi
#ACCESS_KEY=$1
#SECRET_KEY=$2
VIDEO_SRC=$1
USER=$2
PASS=$3
STREAM_NAME=$4
REGION=$5
FPS=$6

# Create a temporary filename in /tmp directory
#jar_files=$(mktemp)
# Create classpath string of dependencies from the local repository to a file
#mvn -Dmdep.outputFile=$jar_files dependency:build-classpath
#export LD_LIBRARY_PATH=/opt/amazon-kinesis-video-streams-producer-sdk-cpp/kinesis-video-native-build/downloads/local/lib:$LD_LIBRARY_PATH
#classpath_values=$(cat $jar_files)
# Start the demo app
# -Daws.accessKeyId=${ACCESS_KEY} -Daws.secretKey=${SECRET_KEY} 

classpath_values=$(cat ./classpath.txt)
java -classpath target/kvsdemo-1.0-SNAPSHOT.jar:$classpath_values  -Daws.accessKeyId=${AWS_ACCESS_KEY_ID} -Daws.secretKey=${AWS_SECRET_ACCESS_KEY}  -Djava.library.path=$LD_LIBRARY_PATH:/opt/amazon-kinesis-video-streams-producer-sdk-cpp/kinesis-video-native-build brainstech.kvsdemo.OpenCVDemo ${VIDEO_SRC} ${USER} ${PASS} ${STREAM_NAME} ${REGION} ${FPS}

