package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;

import java.io.IOException;
import java.nio.file.Path;

// https://fishshell.com/docs/current/fish_for_bash_users.html#variables
// https://fishshell.com/docs/current/tutorial.html#path

public class FishInitGenerator implements ShellInitGenerator {

  private final Path initBash;
  private final String provisioRootRelativeToUserHome;

  public FishInitGenerator(Path shellInit, String provisioRootRelativeToUserHome) {
    this.initBash = shellInit;
    this.provisioRootRelativeToUserHome = provisioRootRelativeToUserHome;
  }

  @Override
  public void preamble() throws IOException {
    touch(initBash);
    line(initBash, "set -gx PROVISIO_ROOT $HOME/%s%n", provisioRootRelativeToUserHome);
    line(initBash, "set -gx PROVISIO_BIN $PROVISIO_ROOT%n");
    line(initBash, "set -gx PROVISIO_INSTALLS $PROVISIO_ROOT/bin/installs%n");
    line(initBash, "set -gx PROVISIO_PROFILES $PROVISIO_ROOT/bin/profiles%n");
    line(initBash, "set -gx PROVISIO_ACTIVE_PROFILE $PROVISIO_ROOT/bin/profiles/profile%n");
    line(initBash, "set -gx PATH $PROVISIO_BIN $PROVISIO_ACTIVE_PROFILE $PATH%n%n");
  }

  @Override
  public void write(String contents) throws IOException {
    line(initBash, contents + "%n");
  }

  @Override
  public void comment(String text) throws IOException {
    line(initBash, "# -------------- " + text + "  --------------%n");
  }

  @Override
  public void pathWithExport(String toolRoot, String pathToExport) throws IOException {
    line(initBash, "set -gx %s $PROVISIO_INSTALLS/%s%n", toolRoot, pathToExport);
    line(initBash, "set -gx PATH $%s $PATH%n%n", toolRoot);
  }
}
