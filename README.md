# Tucana
This is a project from Data Science Engineering team at [24]7.ai for creating a service for deploying a combination of Spark models using [Mleap technology](https://github.com/combust/mleap) and generic rules (in Javascript or Python) in production. The library is built on top of [akka-HTTP](https://doc.akka.io/docs/akka-http/current/index.html). Currently the server supports two operations: returning the predicted class with maximum probability and top N classes and their probabilities

## Dependencies
0. JDK 8. OpenJDK 8 can be used without any issues.
1. Scala: Version 2.11.12
    - The binaries should be in the path
2. Maven: Version 3.3.9 or above
    - The binaries should be in the path
3. [Optional, for development] IntelliJ IDEA Community Edition

## Running with docker

### Installing docker
Following are the links for docker installation.
1. [Ubuntu/Debian](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)
2. [CentOS](https://docs.docker.com/engine/installation/linux/docker-ce/centos/)
3. [Windows](https://docs.docker.com/toolbox/toolbox_install_windows/)

### Building and packaging
1. `cd Tucana`
2. `mvn clean package -DskipTests`

### Running Tucana tests
1. `cd ../Docker`
2. To run the complete test suite:
 
     `./run-docker.sh "test"`

### Using docker shell with Tucana
The following commands would drop you into a docker bash shell with all ModelServer dependencies ready. There you can run other usual commands.

1. `cd ../docker`
2. `./run-docker.sh`

It is also possible to mount a host folder with your data into the docker container, and use those files and folders. For example, to load `/home/abc` from host to docker, use the following command:

	 ./run-docker.sh -m /home/abc
	 
Once you are inside the container, this folder will be available at `/tucana/project` inside the container.
