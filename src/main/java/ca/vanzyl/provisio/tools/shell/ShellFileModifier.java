package ca.vanzyl.provisio.tools.util;

/*
We want to be able to inject and remove the provisio stanza from shell initialization files.

#---- provisio-start ----
source ${HOME}/.provisio/.bin/profile/.init.bash
#---- provisio-end ----
*/

import static ca.vanzyl.provisio.tools.util.ShellFileModifier.Shell.*;
import static java.nio.file.Files.copy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellFileModifier {

  public enum Shell {
    BASH("bash"),
    ZSH("zsh"),
    FISH("fish");

    private final String id;

    Shell(String id) {
      this.id = id;
    }

    public String id() {
      return id;
    }
  }

  public final static String BEGIN_PROVISIO_STANZA = "#---- provisio-start ----";
  public final static String PROVISIO_STANZA_BODY = "source ${HOME}/.provisio/bin/profiles/profile/.init.bash";
  public final static String END_PROVISIO_STANZA = "#---- provisio-end ----";

  // Search order for shell initialization scripts
  //
  // ${HOME}/.bash_profile
  // ${HOME}/.bash_login
  // ${HOME}/.zprofile
  // ${HOME}/.zshrc

  // TODO: make sure this conforms to most shells read shell-init-files.md

  private final static String[] bashInitScripts = new String[]{
      ".bash_profile",
      ".bash_login",
      ".bashrc"
  };

  private final static String[] zshInitScripts = new String[]{
      ".zprofile",
      ".zshrc"
  };

  private final static String[] fishInitScripts = new String[]{
      ".config/fish/config.fish"
  };

  private final Path userHomeDirectory;
  private final Path provisioRoot;

  public ShellFileModifier(Path userHomeDirectory, Path provisioRoot) {
    this.userHomeDirectory = userHomeDirectory;
    this.provisioRoot = provisioRoot;
  }

  private Path bashInitScript() {
    return Arrays.stream(bashInitScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  private Path zshInitScript() {
    return Arrays.stream(zshInitScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  private Path fishInitScript() {
    return Arrays.stream(fishInitScripts)
        .map(userHomeDirectory::resolve)
        .filter(Files::exists)
        .findFirst()
        .orElse(null);
  }

  public static Shell userShell() {
    String userShell = System.getenv("SHELL");
    if (userShell == null) {
      return BASH;
    } else if (userShell.endsWith(BASH.id())) {
      System.out.println("Detected the use of BASH");
      return BASH;
    } else if (userShell.endsWith(ZSH.id())) {
      System.out.println("Detected the use of ZSH");
      return ZSH;
    } else if(userShell.endsWith(FISH.id())) {
      System.out.println("Detected the use of FISH");
      return FISH;
    }
    throw new RuntimeException("Cannot find supported shell initialization script. Only bash, zsh and fish are supported.");
  }

  // During a docker build is no SHELL envar so we'll assume BASH
  public Path findShellInitializationFile() {
    String userShell = System.getenv("SHELL");
    if (userShell == null) {
      return bashInitScript();
    } else if (userShell.endsWith(BASH.id())) {
      System.out.println("Detected the use of BASH");
      return bashInitScript();
    } else if (userShell.endsWith(ZSH.id())) {
      System.out.println("Detected the use of ZSH");
      return zshInitScript();
    } else if(userShell.endsWith(FISH.id())) {
      System.out.println("Detected the use of FISH");
      return fishInitScript();
    }
    throw new RuntimeException("Cannot find supported shell initialization script. Only bash, zsh and fish are supported.");
  }

  public void updateShellInitializationFile() throws IOException {
    System.out.println();
    Path shellFile = findShellInitializationFile();
    writeShellFileBackup(shellFile);
    String shellFileContents = Files.readString(shellFile);
    // Remove and then insert just to be safe
    writeShellFile(shellFile, insertProvisioStanza(removeProvisioStanza(shellFileContents)));
    System.out.println("Updated: " + shellFile);
  }

  public String insertProvisioStanza(String content) {
    return new StringBuilder()
        .append(BEGIN_PROVISIO_STANZA)
        .append(System.lineSeparator())
        .append(PROVISIO_STANZA_BODY)
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
