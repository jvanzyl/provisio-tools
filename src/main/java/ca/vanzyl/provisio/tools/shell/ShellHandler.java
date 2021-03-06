package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.shell.ShellHandler.Shell.*;

import java.io.IOException;
import java.nio.file.Path;

public interface ShellHandler {
  String shellTemplateName();
  String[] shellInitScripts();
  String provisioShellInitializationScript();
  void preamble() throws IOException;
  void pathWithExport(String toolRoot, String pathToExport, String exportedPaths) throws IOException;
  void write(String contents) throws IOException;
  void comment(String text) throws IOException;
  Path updateShellInitialization() throws IOException;

  enum Shell {
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

  // During a docker build is no SHELL envar so we'll assume BASH
  static Shell userShell() {
    String userShell = System.getenv("SHELL");
    if (userShell == null) {
      return BASH;
    } else if (userShell.endsWith(BASH.id())) {
      return BASH;
    } else if (userShell.endsWith(ZSH.id())) {
      return ZSH;
    } else if (userShell.endsWith(FISH.id())) {
      return FISH;
    }
    throw new RuntimeException("Unsupported shell: only bash, zsh and fish are supported.");
  }
}
