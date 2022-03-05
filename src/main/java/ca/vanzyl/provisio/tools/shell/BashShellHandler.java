package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Path;

public class BashShellHandler extends ShellHandlerSupport {

  public static final String SHELL_TEMPLATE = "bash-template.txt";

  private final static String[] bashInitScripts = new String[]{
      ".bash_profile",
      ".bash_login",
      ".bashrc"
  };

  public BashShellHandler(Path userHome, ProvisioningRequest request) {
    super(userHome, request);
  }

  @Override
  public String shellTemplateName() {
    return SHELL_TEMPLATE;
  }

  @Override
  public String[] shellInitScripts() {
    return bashInitScripts;
  }

  @Override
  public void preamble() throws IOException {
    touch(shellInitScript);
    line(shellInitScript, "export PROVISIO_ROOT=${HOME}/%s%n", provisioRootRelativeToUserHome);
    line(shellInitScript, "export PROVISIO_BIN=${PROVISIO_ROOT}%n");
    line(shellInitScript, "export PROVISIO_INSTALLS=${PROVISIO_ROOT}/bin/installs%n");
    line(shellInitScript, "export PROVISIO_PROFILES=${PROVISIO_ROOT}/bin/profiles%n");
    line(shellInitScript, "export PROVISIO_ACTIVE_PROFILE=${PROVISIO_ROOT}/bin/profiles/profile%n");
    line(shellInitScript, "export PATH=${PROVISIO_BIN}:${PROVISIO_ACTIVE_PROFILE}:${PATH}%n%n");
  }

  @Override
  public void pathWithExport(String toolRoot, String relativeToolInstallationPath, String exportedPaths) throws IOException {
    line(shellInitScript, "export %s=${PROVISIO_INSTALLS}/%s%n", toolRoot, relativeToolInstallationPath);
    for (String exportedPath : exportedPaths.split(",")) {
      if (exportedPath.equals(".")) {
        line(shellInitScript, "export PATH=${%s}:${PATH}%n", toolRoot);
      } else {
        line(shellInitScript, "export PATH=${%s}/%s:${PATH}%n", toolRoot, exportedPath.trim());
      }
    }
    line(shellInitScript, "%n");
  }
}
