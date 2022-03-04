package ca.vanzyl.provisio.tools.shell;

import static java.nio.file.Files.copy;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZshInitGenerator extends BashInitGenerator {

  private final static String[] zshInitScripts = new String[]{
      ".zprofile",
      ".zshrc"
  };

  public ZshInitGenerator(Path userHome, ProvisioningRequest request) {
    super(userHome, request);
  }

  // Modification

  private Path zshInitScript() {
    return Arrays.stream(zshInitScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  // During a docker build is no SHELL envar so we'll assume BASH
  public Path findShellInitializationFile() {
    return zshInitScript();
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
