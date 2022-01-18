#!/usr/bin/env bash

profileBinDirectory=${3}
version=${6}
installLocation=${8}
relativeInstallLocation=${12}

read -r -d '' vscodeScript <<EOF
#!/usr/bin/env bash
[ "\${1}" = "--version" ] && echo "${version}" && exit
cd "\${BASH_SOURCE%/*}"
open ${relativeInstallLocation}/VSCodium.app
EOF

echo "${vscodeScript}" > ${profileBinDirectory}/vscode
chmod +x ${profileBinDirectory}/vscode
