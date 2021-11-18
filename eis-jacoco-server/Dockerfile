FROM openjdk:8u212-jdk-slim
LABEL maintainer="eis_engineering_ci_team@eisgroup.com"
SHELL ["/bin/bash", "-c"]
RUN adduser --shell /bin/bash --uid 10001 --gid 0 --disabled-password --gecos 'eis_user' eis \
    && install -d -g root -o eis /opt && chown -R eis:root /opt \
    && echo "export JAVA_HOME=$JAVA_HOME\n" \
"export PATH=$PATH\n" 

USER eis
RUN mkdir -p "/opt/JACOCO_SERVER/data"
COPY --chown=eis:eis target/JacocoServer-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/JACOCO_SERVER/JacocoServer.jar

EXPOSE 8010 6300
VOLUME "/opt/JACOCO_SERVER/data"
WORKDIR /opt/JACOCO_SERVER
CMD nohup java -DPORT_SERVICE=8010 -jar /opt/JACOCO_SERVER/JacocoServer.jar> >(tee nohup.out) 2>&1
