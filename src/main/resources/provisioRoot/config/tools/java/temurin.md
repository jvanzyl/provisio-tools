# Temurin

# arch (x64 | aarch64)
# heap_size (default to normal)
# image_type (default to jdk)
# jvm_impl (default to hotspot)
# os
# release_name
# vendor (default to adoptium)
# project (default to jdk)

# 8
# https://api.adoptium.net/v3/binary/version/jdk8u302-b08/linux/x64/jdk/hotspot/normal/adoptium?project=jdk
# tranlate to:
# https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u302-b08/OpenJDK8U-jdk_x64_linux_hotspot_8u302b08.tar.gz

# https://api.adoptium.net/v3/binary/version/jdk8u302-b08/mac/x64/jdk/hotspot/normal/adoptium?project=jdk
# translate to:
# https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u302-b08/OpenJDK8U-jdk_x64_mac_hotspot_8u302b08.tar.gz

# 11
# https://api.adoptium.net/v3/binary/version/jdk-11.0.12%2B7/linux/x64/jdk/hotspot/normal/adoptium?project=jdk
# translate to:
# https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.12%2B7/OpenJDK11U-jdk_x64_linux_hotspot_11.0.12_7.tar.gz

# https://api.adoptium.net/v3/binary/version/jdk-11.0.12%2B7/mac/x64/jdk/hotspot/normal/adoptium?project=jdk
# translate to:
# https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.12%2B7/OpenJDK11U-jdk_x64_mac_hotspot_11.0.12_7.tar.gz

# 16
# https://api.adoptium.net/v3/binary/version/jdk-16.0.2%2B7/linux/x64/jdk/hotspot/normal/adoptium?project=jdk
# translate to:
# https://github.com/adoptium/temurin16-binaries/releases/download/jdk-16.0.2%2B7/OpenJDK16U-jdk_x64_linux_hotspot_16.0.2_7.tar.gz

# https://api.adoptium.net/v3/binary/version/jdk-16.0.2%2B7/mac/x64/jdk/hotspot/normal/adoptium?project=jdk
# translate to:
# https://github.com/adoptium/temurin16-binaries/releases/download/jdk-16.0.2%2B7/OpenJDK16U-jdk_x64_mac_hotspot_16.0.2_7.tar.gz

# urlTemplate: https://api.adoptium.net/v3/binary/version/{version}/{os}/{arch}/{image_type=jdk|jre}/{jvm_impl=hotspot}/{heap_size=normal|large}/{vendor=eclipse}?project={project=jdk|valhalla|metropolis|jfr|shenandoah}