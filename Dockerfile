FROM quay.io/quarkus/quarkus-micro-image:1.0

ARG VERSION
ARG USER

COPY target/provisio-tools-${VERSION}-runner /root/.provisio/provisio
