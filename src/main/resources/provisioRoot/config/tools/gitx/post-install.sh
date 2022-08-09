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
open ${relativeInstallLocation}/GitX.app
EOF

mkdir -p ${profileBinDirectory} > /dev/null 2>&1
echo "${startupScript}" > ${profileBinDirectory}/gitx
chmod +x ${profileBinDirectory}/gitx

if [ "${os}" = "Darwin" ] && [ "${arch}" = "arm64" ]; then
  # For M1 Mac users, arm64 builds need to have the quarantine bit turned off
  sudo xattr -r -d com.apple.quarantine "${installLocation}/GitX.app"
fi


