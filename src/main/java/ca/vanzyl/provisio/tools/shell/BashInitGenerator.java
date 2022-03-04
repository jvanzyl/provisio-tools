package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;
import static java.nio.file.Files.copy;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BashInitGenerator implements ShellInitGenerator {

  public final static String BEGIN_PROVISIO_STANZA = "#---- provisio-start ----";
  public final static String PROVISIO_STANZA_BODY = "source ${HOME}/.provisio/bin/profiles/profile/.init.bash";
  public final static String END_PROVISIO_STANZA = "#---- provisio-end ----";
  public static final String SHELL_TEMPLATE = "bash-template.txt";

  private final static String[] bashInitScripts = new String[]{
      ".bash_profile",
      ".bash_login",
      ".bashrc"
  };

  protected final Path initBash;
  protected final String provisioRootRelativeToUserHome;
  protected final Path userHomeDirectory;

  public BashInitGenerator(Path userHome, ProvisioningRequest request) {
    this.userHomeDirectory = userHome;
    this.initBash = request.binaryProfileDirectory().resolve(SHELL_TEMPLATE);
    this.provisioRootRelativeToUserHome = userHome.relativize(request.provisioRoot()).toString();;
  }

  @Override
  public void preamble() throws IOException {
    touch(initBash);
    line(initBash, "export PROVISIO_ROOT=${HOME}/%s%n", provisioRootRelativeToUserHome);
    line(initBash, "export PROVISIO_BIN=${PROVISIO_ROOT}%n");
    line(initBash, "export PROVISIO_INSTALLS=${PROVISIO_ROOT}/bin/installs%n");
    line(initBash, "export PROVISIO_PROFILES=${PROVISIO_ROOT}/bin/profiles%n");
    line(initBash, "export PROVISIO_ACTIVE_PROFILE=${PROVISIO_ROOT}/bin/profiles/profile%n");
    line(initBash, "export PATH=${PROVISIO_BIN}:${PROVISIO_ACTIVE_PROFILE}:${PATH}%n%n");
  }

  @Override
  public void write(String contents) throws IOException {
    line(initBash, contents + "%n");
  }

  @Override
  public void comment(String text) throws IOException {
    line(initBash, "# -------------- " + text + "  --------------%n");
  }

  //
  // # -------------- pulumi  --------------
  // PULUMI_ROOT=${PROVISIO_INSTALLS}/pulumi/3.22.1
  // export PATH=${PULUMI_ROOT}:${PATH}
  //

  @Override
  public void pathWithExport(String toolRoot, String relativeToolInstallationPath, String exportedPaths) throws IOException {
    line(initBash, "export %s=${PROVISIO_INSTALLS}/%s%n", toolRoot, relativeToolInstallationPath);
    for (String exportedPath : exportedPaths.split(",")) {
      if (exportedPath.equals(".")) {
        line(initBash, "export PATH=${%s}:${PATH}%n", toolRoot);
      } else {
        line(initBash, "export PATH=${%s}/%s:${PATH}%n", toolRoot, exportedPath.trim());
      }
    }
    line(initBash, "%n");
  }

  @Override
  public String shellTemplateName() {
    return SHELL_TEMPLATE;
  }

  // Modification

  private Path bashInitScript() {
    return Arrays.stream(bashInitScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  // During a docker build is no SHELL envar so we'll assume BASH
  public Path findShellInitializationFile() {
    return bashInitScript();
  }

  public void updateShellInitialization() throws IOException {
    System.out.println();
    Path shellFile = findShellInitializationFile();
    writeShellFileBackup(shellFile);
    String shellFileContents = Files.readString(shellFile);
    writeShellFile(shellFile, insertProvisioStanza(removeProvisioStanza(shellFileContents)));
    System.out.println("Updated: " + shellFile);
  }

  public String insertProvisioStanza(String content) {
    return BEGIN_PROVISIO_STANZA
        + System.lineSeparator()
        + PROVISIO_STANZA_BODY
        + System.lineSeparator()
        + END_PROVISIO_STANZA
        + System.lineSeparator()
        + content;
  }

  public String removeProvisioStanza(String content) {
    Pattern pattern = Pattern.compile(BEGIN_PROVISIO_STANZA + ".*" + END_PROVISIO_STANZA + "\\s*", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);
    return matcher.replaceAll("");
  }

  private void writeShellFileBackup(Path shellFile) throws IOException {
    Path backup = shellFile.resolveSibling(shellFile.getFileName() + ".provisio_backup");
    copy(shellFile, backup, StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeShellFile(Path shellFile, String content) throws IOException {
    Files.writeString(shellFile, content);
  }
}
