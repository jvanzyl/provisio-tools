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
import java.util.List;
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
  public void provisioStanzaRemovalFromShellInitializationFile() throws Exception {
    String shellFileContents = createFileContentsWith(
        "# first",
        BEGIN_PROVISIO_STANZA,
        PROVISIO_STANZA_BODY,
        END_PROVISIO_STANZA,
        "# last");
    Path shellFile = target("bash_profile-result-no-provisio");
    Files.writeString(shellFile, shellFileContents);
    modifier.removeProvisioFromShellFile(shellFile);
    List<String> a = Files.readAllLines(shellFile);
    assertThat(linesOf(shellFile.toFile())).containsExactly(
      "# first",
      "# last"
    );
  }

  @Test
  public void provisioStanzaInsertionIntoShellInitializationContent() throws Exception {
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
  public void provisioStanzaInsertionIntoShellInitializationFile() throws Exception {
    String shellFileContents = createFileContentsWith(
        "# first",
        "# last"
    );
    Path shellFile = target("bash_profile-result-with-provisio");
    Files.writeString(shellFile, shellFileContents);
    modifier.insertProvisioIntoShellFile(shellFile);
    List<String> a = Files.readAllLines(shellFile);
    assertThat(linesOf(shellFile.toFile())).containsExactly(
        BEGIN_PROVISIO_STANZA,
        PROVISIO_STANZA_BODY,
        END_PROVISIO_STANZA,
        "# first",
        "# last"
    );
  }

  protected String createFileContentsWith(String... lines) {
    StringBuilder builder = new StringBuilder();
    Arrays.stream(lines).forEach(l -> builder.append(l).append(System.lineSeparator()));
    return builder.toString();
  }

  protected Path shellFile(String name) {
    return test().resolve("shell").resolve(name);
  }
}
