package ca.vanzyl.provisio.tools.shell;

import java.io.IOException;

public interface ShellInitGenerator {
  void preamble() throws IOException;
  void write(String contents) throws IOException;
  void comment(String text) throws IOException;
  void pathWithExport(String toolRoot, String pathToExport) throws IOException;
}
