FROM jenkins/jenkins:lts

USER 0

RUN apt-get update && apt-get install -y \
    docker.io \
    maven \
    curl \
    unzip \
    jq \
    netcat-openbsd

RUN jenkins-plugin-cli --plugins \
    workflow-aggregator \
    git \
    docker-workflow \
    pipeline-stage-view \
    sonar \
    credentials-binding \
    blueocean

USER jenkins