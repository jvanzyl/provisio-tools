package ca.vanzyl.provisio.tools.util;

/*
      "${PROVISIO_FUNCTIONS}" \
        "${toolProfileYaml}" \
        "${bin}" \
        "${filename}" \
        "${url}" \
        "${version}" \
        "${id}" \
        "${installLocation}" \
        "${os}" \
        "${arch}" \
        "${profileBinaryDirectory}"

./vscodium/post-install.sh
profileBinDirectory=${3}
version=${6}

./graalvm/post-install.sh
installLocation=${8}
os=${9}

./node/post-install.sh
provisioFunctions=${1}
profileYaml=${2}
installLocation=${8}

./jenv/post-install.sh
provisioFunctions=${1}
profileYaml=${2}
installLocation=${8}
profileBinaryDirectory=${11}

./helm/post-install.sh
source $1
profile=$2
bin=$3

./fzf/post-install.sh
version=${6}
installLocation=${8}

./krew/post-install.sh
provisioFunctions=${1}
profile=${2}
installLocation=${8}
os=${9}
arch=${10}

provisioFunctions=${1}
profile=${2}
profileBinDirectory=${3}


*/

import static ca.vanzyl.provisio.tools.Provisio.POST_INSTALL;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PostInstall {

  private final Path toolDirectory;
  private final List<String> args;

  public PostInstall(Path toolDirectory, List<String> args) {
    this.toolDirectory = toolDirectory;
    this.args = args;
  }

  public void execute() throws Exception {
    CliCommand command = new CliCommand(args, toolDirectory, Map.of(), false);
    CliCommand.Result result = command.execute();
  }
}
