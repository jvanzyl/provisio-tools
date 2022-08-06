#!/usr/bin/env bash

# provisioFunctions=$1
# profileYaml=$2
# profileBinDirectory=$3
# file=$4

source ${1}
profile=${2}
bin=${3}
installLocation=${8}

create_variables ${profile}
export NVM_DIR="${installLocation}"
source "$NVM_DIR/nvm.sh"

for nodejs in ${tools_nvm_nodejs[*]}; do
  nvm install ${nodejs}
done
