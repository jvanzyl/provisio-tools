#!/usr/bin/env bash

provisioFunctions=${1}
profileYaml=${2}
installLocation=${8}
os=${9}
profileBinaryDirectory=${11}

source ${provisioFunctions}
create_variables ${profileYaml}
mkdir -p ${installLocation}/{plugins,versions} 2>&1

for plugin in ${tools_jenv_plugins[*]}; do
  # Make symlinks to the available plugins directory relative so that PROVISIO_ROOT is relocatable
  (cd ${installLocation}/plugins; rm -f ${plugin}; ln -s ../available-plugins/${plugin} ${plugin})
done

# TODO: (multiple-runtimes) Here we are introducing an implicit relationship of jenv on java. The descriptor
# should be changed to reflect this and also order the installation of tools as java needs to be installed
# before jenv can add JDKs

if [ "${os}" = "Darwin" ]; then
  jdkHome="Contents/Home"
elif [ "${os}" = "Linux" ]; then
  jdkHome=""
fi

for jdk in $(ls ${profileBinaryDirectory}/java); do
  ${installLocation}/bin/jenv add "${profileBinaryDirectory}/java/${jdk}/${jdkHome}"
done

for jdk in $(ls ${profileBinaryDirectory}/graalvm); do
  ${installLocation}/bin/jenv add "${profileBinaryDirectory}/graalvm/${jdk}/${jdkHome}"
done

# Set the global version of java if the configuration is present
[ ! -z "${tools_jenv_global}" ] && ${installLocation}/bin/jenv global ${tools_jenv_global}
