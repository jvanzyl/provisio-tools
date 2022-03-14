#!/usr/bin/env bash

# Notes:
# https://stackoverflow.com/questions/64963370/error-cannot-install-in-homebrew-on-arm-processor-in-intel-default-prefix-usr

ARCH="$(uname -m)"
echo "Checking prerequistes for OSX on ${ARCH}..."
if [ "${ARCH}" = "arm64" ]; then
  echo "Installing Rosetta 2 ..."
  /usr/sbin/softwareupdate --install-rosetta --agree-to-license
  brewPrefix="arch -x86_64"
fi

command -v brew > /dev/null 2>&1
if [[ $? != 0 ]]; then
  echo "Brew is not installed."
  ${brewPrefix} /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
fi

# These scripts themselves require realpath and at the very least jenv
# uses realpath as well. You can see what utilities are provided
# by coreutils here:
# 
# http://www.maizure.org/projects/decoded-gnu-coreutils/
brew ls --versions coreutils > /dev/null 2>&1
if [[ $? != 0 ]]; then
  ${brewPrefix} brew install coreutils
fi
