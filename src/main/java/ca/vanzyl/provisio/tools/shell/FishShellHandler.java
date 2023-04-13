package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;
import static java.nio.file.Files.isDirectory;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// https://fishshell.com/docs/current/fish_for_bash_users.html#variables
// https://fishshell.com/docs/current/tutorial.html#path

public class FishShellHandler extends ShellHandlerSupport {

  public static final String SHELL_TEMPLATE = "fish-template.txt";

  private final static String[] fishInitScripts = new String[]{
      ".config/fish/config.fish",
      ".config/fish/conf.d"
  };

  public FishShellHandler(Path userHome, ProvisioningRequest request) {
    super(userHome, request);
  }

  @Override
  public String shellTemplateName() {
    return SHELL_TEMPLATE;
  }

  @Override
  public String[] shellInitScripts() {
    return fishInitScripts;
  }

  @Override
  public void preamble() throws IOException {
    touch(shellInitScript);
    line(shellInitScript, "set -gx PROVISIO_ROOT $HOME/%s%n", provisioRootRelativeToUserHome);
    line(shellInitScript, "set -gx PROVISIO_BIN $PROVISIO_ROOT%n");
    line(shellInitScript, "set -gx PROVISIO_COMMON ${PROVISIO_ROOT}/bin/common%n");
    line(shellInitScript, "set -gx PROVISIO_INSTALLS $PROVISIO_ROOT/bin/installs%n");
    line(shellInitScript, "set -gx PROVISIO_PROFILES $PROVISIO_ROOT/bin/profiles%n");
    line(shellInitScript, "set -gx PROVISIO_ACTIVE_PROFILE $PROVISIO_ROOT/bin/profiles/profile%n");
    line(shellInitScript, "set -gx PATH $PROVISIO_BIN $PROVISIO_COMMON $PROVISIO_ACTIVE_PROFILE $PATH%n%n");
  }

  public void pathWithExport(String toolRoot, String relativeToolInstallationPath, String exportedPaths) throws IOException {
    line(shellInitScript, "set -gx %s $PROVISIO_INSTALLS/%s%n", toolRoot, relativeToolInstallationPath);
    for (String exportedPath : exportedPaths.split(",")) {
      if (exportedPath.equals(".")) {
        line(shellInitScript, "set -gx PATH $%s $PATH%n%n", toolRoot);
      } else {
        line(shellInitScript, "set -gx PATH $%s/%s $PATH%n", toolRoot, exportedPath.trim());
      }
    }
    line(shellInitScript, "%n");
  }

  public Path updateShellInitialization() throws IOException {
    Path shellFile = findShellInitializationFile();
    if (isDirectory(shellFile)) {
      //
      // ~/.config/fish/conf.d/
      //
      Path fishConfig = shellFile.resolve("provisio.fish");
      writeShellFileBackup(fishConfig);
      String shellFileContents = Files.readString(fishConfig);
      writeShellFile(fishConfig, insertProvisioStanza(removeProvisioStanza(shellFileContents)));
      return shellFile;
    } else {
      //
      // ~/.config/fish/config.fish
      //
      writeShellFileBackup(shellFile);
      String shellFileContents = Files.readString(shellFile);
      writeShellFile(shellFile, insertProvisioStanza(removeProvisioStanza(shellFileContents)));
      return shellFile;
    }
  }
}
