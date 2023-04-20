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
open ${relativeInstallLocation}/DBeaver.app
EOF

mkdir -p ${profileBinDirectory} > /dev/null 2>&1
echo "${startupScript}" > ${profileBinDirectory}/dbeaver
chmod +x ${profileBinDirectory}/dbeaver

