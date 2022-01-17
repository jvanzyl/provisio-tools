package ca.vanzyl.provisio.tools.util;

/*
We want to be able to inject and remove the provisio stanza from shell initialization files.

#---- provisio-start ----
source ${HOME}/.provisio/.bin/profile/.init.bash
#---- provisio-end ----
*/

import static java.nio.file.Files.copy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellFileModifier {

  public final static String BEGIN_PROVISIO_STANZA = "#---- provisio-start ----";
  public final static String PROVISIO_STANZA_BODY = "source ${HOME}/.provisio/.bin/profile/.init.bash";
  public final static String END_PROVISIO_STANZA = "#---- provisio-end ----";

  // Search order for shell initialization scripts
  //
  // ${HOME}/.bash_profile
  // ${HOME}/.bash_login
  // ${HOME}/.zprofile
  // ${HOME}/.zshrc

  // TODO: make sure this conforms to most shells read shell-init-files.md

  private final static String[] shellInitializationScripts = new String[]{
      ".bash_profile",
      ".bash_login",
      ".zprofile",
      ".zshrc"
  };

  private final Path userHomeDirectory;
  private final Path provisioRoot;

  public ShellFileModifier(Path userHomeDirectory, Path provisioRoot) {
    this.userHomeDirectory = userHomeDirectory;
    this.provisioRoot = provisioRoot;
  }

  public Path findShellInitializationFile() {
    return Arrays.stream(shellInitializationScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  public void updateShellInitializationFile() throws IOException {
    Path shellFile = findShellInitializationFile();
    writeShellFileBackup(shellFile);
    String shellFileContents = Files.readString(shellFile);
    String s = removeProvisioStanza(shellFileContents);
    String y = insertProvisioStanza(s);
    writeShellFile(shellFile, y);
  }

  public String insertProvisioStanza(String content) {

    String provisioRootRelativeToHome = userHomeDirectory.relativize(provisioRoot).toString();

    return new StringBuilder()
        .append(BEGIN_PROVISIO_STANZA)
        .append(System.lineSeparator())
        .append(String.format("source ${HOME}/%s/bin/profiles/profile/.init.bash", provisioRootRelativeToHome))
        .append(System.lineSeparator())
        .append(END_PROVISIO_STANZA)
        .append(System.lineSeparator())
        .append(content)
        .toString();
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
