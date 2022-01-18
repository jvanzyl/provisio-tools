FROM quay.io/quarkus/quarkus-micro-image:1.0

ARG VERSION
ARG USER

COPY target/provisio-tools-${VERSION}-runner /root/.provisio/provisio
COPY provisio.yaml /root/.provisio/profiles/tools/profile.yaml
RUN cd /root/.provisio && ./provisio provision tools && rm -rf /root/.provisio/bin/cache
