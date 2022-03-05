package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static java.nio.file.Files.copy;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ShellHandlerSupport implements  ShellHandler {

  public final static String BEGIN_PROVISIO_STANZA = "#---- provisio-start ----";
  public final static String PROVISIO_STANZA_BODY = "source ${HOME}/.provisio/bin/profiles/profile/.init.bash";
  public final static String END_PROVISIO_STANZA = "#---- provisio-end ----";

  protected final Path shellInitScript;
  protected final String provisioRootRelativeToUserHome;
  protected final Path userHomeDirectory;

  public ShellHandlerSupport(Path userHome, ProvisioningRequest request) {
    this.userHomeDirectory = userHome;
    this.shellInitScript = request.binaryProfileDirectory().resolve(shellTemplateName());
    this.provisioRootRelativeToUserHome = userHome.relativize(request.provisioRoot()).toString();;
  }

  @Override
  public Path shellInitScript() {
    return shellInitScript;
  }

  @Override
  public void write(String contents) throws IOException {
    line(shellInitScript, contents + "%n");
  }

  @Override
  public void comment(String text) throws IOException {
    line(shellInitScript, "# -------------- " + text + "  --------------%n");
  }

  public void updateShellInitialization() throws IOException {
    Path shellFile = findShellInitializationFile();
    writeShellFileBackup(shellFile);
    String shellFileContents = Files.readString(shellFile);
    writeShellFile(shellFile, insertProvisioStanza(removeProvisioStanza(shellFileContents)));
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

  protected void writeShellFileBackup(Path shellFile) throws IOException {
    Path backup = shellFile.resolveSibling(shellFile.getFileName() + ".provisio_backup");
    copy(shellFile, backup, StandardCopyOption.REPLACE_EXISTING);
  }

  protected void writeShellFile(Path shellFile, String content) throws IOException {
    Files.writeString(shellFile, content);
  }

  public Path findShellInitializationFile() {
    return Arrays.stream(shellInitScripts())
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }
}
