#!/bin/bash -e

# First build the docker image from the dockerfile
sudo docker build -t tucana ./

# Hostname for docker container
dockerHostname="tucana-docker"

predictServerTestJar=`ls Tucana-*-tests.jar`
predictServerMainJar=`ls Tucana*[^tests].jar`

if [[ "$1" == "test" ]]
then
    set -x
      sudo docker run -h $dockerHostname \
          -p 18080:18080 \
            --rm \
            -it \
            --name tucanaContainer \
            --entrypoint /Tucana/run-tucana-tests.sh \
            tucana \
            $predictServerMainJar $predictServerTestJar $@
else
    echo "** Dropping into bash with all dependencies"
    # Using getopts to get command line arguments
    hostFolder=""
    mntFolder="/Tucana/project"
    mountCmd=""
    while getopts ":m:" Opt
    do
      case $Opt in
        m )
          echo "** Mounting folder ${OPTARG} to /project"
          hostFolder=$OPTARG
          mountCmd="--mount type=bind,source=$hostFolder,target=$mntFolder";;
        * ) echo "Unknown option $Opt";;
      esac
    done

     sudo docker run -h $dockerHostname \
          -p 18080:18080 \
          --rm \
          -it \
          --name tucanaContainer \
        $mountCmd \
        --entrypoint /Tucana/run-tucana.sh \
        tucana
fi
