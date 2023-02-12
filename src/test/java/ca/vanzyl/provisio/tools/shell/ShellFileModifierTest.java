package ca.vanzyl.provisio.tools.shell;

import static ca.vanzyl.provisio.tools.shell.ShellHandlerSupport.BEGIN_PROVISIO_STANZA;
import static ca.vanzyl.provisio.tools.shell.ShellHandlerSupport.END_PROVISIO_STANZA;
import static ca.vanzyl.provisio.tools.shell.ShellHandlerSupport.PROVISIO_STANZA_BODY;
import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;

import ca.vanzyl.provisio.tools.ProvisioTestSupport;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class ShellFileModifierTest extends ProvisioTestSupport {

  protected BashShellHandler modifier;
  protected Path homeDirectory;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    homeDirectory = path("homeDirectory");
    modifier = new BashShellHandler(homeDirectory, request);
  }

  @Test
  public void provisioStanzaRemovalFromShellInitializationContent() {
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
    touch(homeDirectory, ".bash_profile");
    touch(homeDirectory, ".bash_login");
    touch(homeDirectory, ".zprofile");
    touch(homeDirectory, ".zshrc");
    Path shellFile = modifier.findShellInitializationFile();
    assertThat(shellFile).hasFileName(".bash_profile");
  }

  @Test
  public void provisioUpdateShellInitializationFile() throws Exception {
    Path shellFile = path(homeDirectory, ".bash_profile");
    String shellFileContents = createFileContentsWith(
        "# first",
        "# last"
    );
    writeString(shellFile, shellFileContents);
    touch(homeDirectory, ".bash_login");
    touch(homeDirectory, ".zprofile");
    touch(homeDirectory, ".zshrc");
    modifier.updateShellInitialization();
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
