package ca.vanzyl.provisio.tools.util;

/*
We want to be able to inject and remove the provisio stanza from shell initialization files.

#---- provisio-start ----
source ${HOME}/.provisio/.bin/profile/.init.bash
#---- provisio-end ----
*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

  public Path findShellInitializationFile(Path homeDirectory) {
    return Arrays.stream(shellInitializationScripts)
        .map(homeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
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

  public void insertProvisioIntoShellFile(Path shellFile) throws IOException {
    String shellFileContents = Files.readString(shellFile);
    String modifiedContent = insertProvisioStanza(shellFileContents);
    writeShellFile(shellFile, modifiedContent);
  }

  public void removeProvisioFromShellFile(Path shellFile) throws IOException {
    String shellFileContents = Files.readString(shellFile);
    String modifiedContent = removeProvisioStanza(shellFileContents);
    writeShellFile(shellFile, modifiedContent);
  }

  public String removeProvisioStanza(String content) {
    Pattern pattern = Pattern.compile(BEGIN_PROVISIO_STANZA + ".*" + END_PROVISIO_STANZA + "\\s*", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);
    return matcher.replaceAll("");
  }

  private void writeShellFile(Path shellFile, String content) throws IOException {
    Files.writeString(shellFile, content);
  }
}
