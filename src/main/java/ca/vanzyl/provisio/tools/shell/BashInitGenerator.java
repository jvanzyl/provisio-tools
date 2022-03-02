package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.util.FileUtils.line;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;

import java.io.IOException;
import java.nio.file.Path;

public class BashInitGenerator implements ShellInitGenerator {

  private final Path initBash;
  private final String provisioRootRelativeToUserHome;

  public BashInitGenerator(Path shellInit, String provisioRootRelativeToUserHome) {
    this.initBash = shellInit;
    this.provisioRootRelativeToUserHome = provisioRootRelativeToUserHome;
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

  @Override
  public void pathWithExport(String toolRoot, String pathToExport) throws IOException {
    line(initBash, toolRoot + "=${PROVISIO_INSTALLS}/%s%n", pathToExport);
    line(initBash, "export PATH=${%s}:${PATH}%n%n", toolRoot);
  }
}
