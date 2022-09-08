FROM eclipse-temurin:17.0.4_8-jre

ARG MAVEN_VERSION

COPY target/provisio-tools-${MAVEN_VERSION}-runner.jar /root/.provisio/provisio.jar
