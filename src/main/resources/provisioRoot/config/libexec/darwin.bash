#!/usr/bin/env bash

PROVISIO_ROOT="${1:-$HOME/.provisio}"
echo "Checking prerequistes for OSX ..."
if [ ! -f ${PROVISIO_ROOT}/bin/common/coreutils ]; then
  echo "GNU coreutils is not installed. Installing GNU coreutils ..."
  arch="$(uname -m)"
  if [ "${arch}" = "arm64" ]; then
    if [ ! -f /Library/Apple/usr/lib/libRosettaAot.dylib ]; then
      # Install Rosetta if it's present, still lots of x86_64 binaries
      echo "Machine is an M1 Mac. Installing Rosetta 2 ..."
      /usr/sbin/softwareupdate --install-rosetta --agree-to-license
    fi
  fi
  # ----------------------------------------------------------------------------
  # Rust-based GNU coreutils that provides things like readlink:
  # https://github.com/uutils/coreutils
  # ----------------------------------------------------------------------------
  version="0.0.12"
  base="coreutils-${version}-x86_64-apple-darwin"
  tgz="${base}.tar.gz"
  cachePath="${PROVISIO_ROOT}/bin/cache/coreutils/${version}"
  tgzPath="${cachePath}/${tgz}"
  mkdir -p ${cachePath} > /dev/null 2>&1
  curl -sL -o ${tgzPath} https://github.com/uutils/coreutils/releases/download/${version}/${tgz}
  commonBin="${PROVISIO_ROOT}/bin/common"
  mkdir -p ${commonBin} > /dev/null 2>&1
  tar -xvf ${tgzPath} --strip-components=1 -C ${commonBin} ${base}/coreutils
  spctl --add "${commonBin}/coreutils"
fi
