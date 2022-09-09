package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Path;

public class PathHandler extends ShellHandlerSupport {

  public PathHandler(Path userHome, ProvisioningRequest request) {
    super(userHome, request);
  }

  @Override
  public String shellTemplateName() {
    return null;
  }

  @Override
  public String[] shellInitScripts() {
    return null;
  }

  @Override
  public String provisioShellInitializationScript() {
    return "PATH";
  }

  @Override
  public void preamble() throws IOException {
    touch(shellInitScript);
    line(shellInitScript, "%s/%s/bin/profiles/profile%n", userHomeDirectory, provisioRootRelativeToUserHome);
  }

  @Override
  public void pathWithExport(String toolRoot, String relativeToolInstallationPath, String exportedPaths) throws IOException {
    for (String exportedPath : exportedPaths.split(",")) {
      if (exportedPath.equals(".")) {
        line(shellInitScript, "%s/%s/bin/installs/%s%n", userHomeDirectory, provisioRootRelativeToUserHome, relativeToolInstallationPath);
      } else {
        line(shellInitScript, "%s/%s/bin/installs/%s/%s%n", userHomeDirectory, provisioRootRelativeToUserHome, relativeToolInstallationPath, exportedPath.trim());
      }
    }
  }
}
