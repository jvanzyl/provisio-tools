#!/usr/bin/env bash

profileBinDirectory=${3}
version=${6}
installLocation=${8}
os=${9}
arch=${10}
relativeInstallLocation=${12}

read -r -d '' startupScript <<EOF
#!/usr/bin/env bash
cd "\${BASH_SOURCE%/*}"
open ${relativeInstallLocation}/OpenLens.app
EOF

mkdir -p ${profileBinDirectory} > /dev/null 2>&1
echo "${startupScript}" > ${profileBinDirectory}/openlens
chmod +x ${profileBinDirectory}/openlens

# The weird arch name is from the weird naming of the openlens packages: https://github.com/MuhammedKalkan/OpenLens/issues/22
if [ "${os}" = "darwin" ] && [ "${arch}" = "-arm64" ]; then
  # For M1 Mac users, arm64 builds need to have the quarantine bit turned off
  echo "Running xattr -r -d com.apple.quarantine ${relativeInstallLocation}/OpenLens.app for M1 application ..."
  sudo xattr -r -d com.apple.quarantine "${installLocation}/OpenLens.app"
fi


