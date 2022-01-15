package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.ShellFileModifier.BEGIN_PROVISIO_STANZA;
import static ca.vanzyl.provisio.tools.util.ShellFileModifier.END_PROVISIO_STANZA;
import static ca.vanzyl.provisio.tools.util.ShellFileModifier.PROVISIO_STANZA_BODY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;

import ca.vanzyl.provisio.tools.util.ShellFileModifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class ShellFileModifierTest extends ProvisioTestSupport {

  protected ShellFileModifier modifier;

  @Before
  public void setUp() {
    modifier = new ShellFileModifier();
  }

  @Test
  public void provisioStanzaRemovalFromShellInitializationContent() throws Exception {
    String shellFileContent = createFileContentsWith(
        "# first",
        BEGIN_PROVISIO_STANZA,
        PROVISIO_STANZA_BODY,
        END_PROVISIO_STANZA,
        "# last");
    String modified = modifier.removeProvisioStanza(shellFileContent);
    assertThat(modified.split(System.lineSeparator())).containsExactly("# first", "# last");
  }

  @Test
  public void provisioStanzaInsertionIntoShellInitializationContent() {
    String shellFileContent = createFileContentsWith(
      "# first",
      "# last"
    );
    String modified = modifier.insertProvisioStanza(shellFileContent);
    assertThat(modified.split(System.lineSeparator())).containsExactly(
        BEGIN_PROVISIO_STANZA,
        PROVISIO_STANZA_BODY,
        END_PROVISIO_STANZA,
        "# first",
        "# last"
    );
  }

  @Test
  public void findingCorrectShellInitializationFile() throws Exception {
    touch("target/shell/.bash_profile");
    touch("target/shell/.bash_login");
    touch("target/shell/.zprofile");
    touch("target/shell/.zshrc");
    Path shellFile = modifier.findShellInitializationFile(target("target/shell"));
    assertThat(shellFile).hasFileName(".bash_profile");
  }

  @Test
  public void provisioUpdateShellInitializationFile() throws Exception {
    Path shellFile = target("shell/.bash_profile");
    String shellFileContents = createFileContentsWith(
        "# first",
        "# last"
    );
    Files.writeString(shellFile, shellFileContents);
    touch("shell/.bash_login");
    touch("shell/.zprofile");
    touch("shell/.zshrc");
    modifier.updateShellInitializationFile(target("shell"));
    assertThat(linesOf(shellFile.toFile())).containsExactly(
        BEGIN_PROVISIO_STANZA,
        PROVISIO_STANZA_BODY,
        END_PROVISIO_STANZA,
        "# first",
        "# last"
    );
    assertThat(shellFileBackup(shellFile)).exists();
  }

  protected Path shellFileBackup(Path shellFile) {
    return shellFile.resolveSibling(shellFile.getFileName() + ".provisio_backup");
  }

  protected String createFileContentsWith(String... lines) {
    StringBuilder builder = new StringBuilder();
    Arrays.stream(lines).forEach(l -> builder.append(l).append(System.lineSeparator()));
    return builder.toString();
  }
}
