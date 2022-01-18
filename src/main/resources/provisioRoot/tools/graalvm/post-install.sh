#!/usr/bin/env bash

installLocation=${8}
os=${9}

if [ "${os}" = "darwin" ]; then
  if [ ! -L "${installLocation}/Contents/Home/bin/native-image" ]; then
    ${installLocation}/Contents/Home/bin/gu install native-image
  fi
elif [ "${os}" = "linux" ]; then
  if [ ! -L "${installLocation}/bin/native-image" ]; then
    ${installLocation}/bin/gu install native-image
  fi
fi
