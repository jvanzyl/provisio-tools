package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioningTestSupport {

  protected Provisio provisio;
  protected Path provisioRoot = Paths.get("target", "provisio");
  protected Path cacheDirectory = provisioRoot.resolve("bin").resolve("cache");
  protected Path installsDirectory = provisioRoot.resolve("bin").resolve("installs");
  protected Path profilesDirectory = provisioRoot.resolve("bin").resolve("profiles");

  @Before
  public void setUp() throws Exception {
    boolean useLocalCache = true;
    resetDirectory(installsDirectory);
    if(useLocalCache) {
      provisio = new Provisio(Provisio.cache, installsDirectory, profilesDirectory, "jvanzyl");
    } else {
      provisio = new Provisio(provisioRoot, "jvanzyl");
    }
  }
}
