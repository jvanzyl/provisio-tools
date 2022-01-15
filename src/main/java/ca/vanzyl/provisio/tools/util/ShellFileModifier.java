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

  private final static String[] shellInitializationScripts = new String[]{
      ".bash_profile",
      ".bash_login",
      ".zprofile",
      ".zshrc"
  };

  public Path findShellInitializationFile() {
    return findShellInitializationFile(Paths.get(System.getProperty("user.home")));
  }

  public Path findShellInitializationFile(Path homeDirectory) {
    return Arrays.stream(shellInitializationScripts)
        .map(homeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  public void updateShellInitializationFile(Path homeDirectory) throws IOException {
    Path shellFile = findShellInitializationFile(homeDirectory);
    writeShellFileBackup(shellFile);
    String shellFileContents = Files.readString(shellFile);
    String s = removeProvisioStanza(shellFileContents);
    String y = insertProvisioStanza(s);
    writeShellFile(shellFile, y);
  }

  public void updateShellInitializationFile() throws IOException {
    updateShellInitializationFile(Paths.get(System.getProperty("user.home")));
  }

  public String insertProvisioStanza(String content) {
    return new StringBuilder()
        .append(BEGIN_PROVISIO_STANZA)
        .append(System.lineSeparator())
        .append("source ${HOME}/.provisio/.bin/profile/.init.bash")
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
