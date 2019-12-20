FROM centos:latest

USER root

RUN yum clean all; \
    rpm --rebuilddb; \
    yum install -y initscripts curl which tar sudo rsync openssh-server openssh-clients

#############################################################################
# Install java
RUN yum install -y java-1.8.0-openjdk
ENV JAVA_HOME=/etc/alternatives/jre

#############################################################################
# Install Scala
ARG SCALA_VERSION=2.11.12
ENV SCALA_HOME=/usr/local/scala-$SCALA_VERSION
RUN mkdir -p "${SCALA_HOME}" \
  && export SCALA_ARCHIVE=scala-$SCALA_VERSION.tgz \
  && export SCALA_DOWNLOAD_PATH=scala/$SCALA_VERSION/$SCALA_ARCHIVE \
  && curl -#SL https://downloads.lightbend.com/$SCALA_DOWNLOAD_PATH | \
    tar -xz -C $SCALA_HOME --strip-components 1 \
  && rm -rf $SCALA_ARCHIVE

RUN rm $SCALA_HOME/lib/akka-actor_2.11-*.jar

ENV PATH=$PATH:$SCALA_HOME/bin

RUN mkdir /Tucana

COPY Tucana-*.jar /Tucana/
COPY flashml-noPage.zip /Tucana/
COPY predict-server-config.conf /Tucana/
COPY run-tucana-tests.sh /Tucana/run-tucana-tests.sh
RUN chmod 777 /Tucana/run-tucana-tests.sh

COPY scalatest_2.11-3.0.5.jar /Tucana/scalatest_2.11-3.0.5.jar
COPY scalactic_2.11-3.0.5.jar /Tucana/scalactic_2.11-3.0.5.jar

WORKDIR /Tucana

