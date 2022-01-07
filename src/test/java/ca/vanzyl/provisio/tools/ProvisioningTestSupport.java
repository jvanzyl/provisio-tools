package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.FileUtils.resetDirectory;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioningTestSupport {

  protected Provisio provisio;
  protected Path target = Paths.get("target", "provisio");
  protected Path cacheDirectory = target.resolve("cache");
  protected Path binaryDirectory = target.resolve("bin");

  @Before
  public void setUp() throws Exception {
    resetDirectory(binaryDirectory);
    provisio = new Provisio(cacheDirectory, binaryDirectory);
  }
}
