package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectory;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.cachePathFor;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.interpolateToolPath;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioTestSupport {

  protected Provisio provisio;
  protected Path realProvisioRoot = Paths.get(System.getProperty("user.home"), ".provisio");
  protected Path provisioRoot = Paths.get("target", "provisio");
  protected Path cacheDirectory = provisioRoot.resolve("bin").resolve("cache");
  protected Path installsDirectory = provisioRoot.resolve("bin").resolve("installs");
  protected Path profilesDirectory = provisioRoot.resolve("bin").resolve("profiles");

  @Before
  public void setUp() throws Exception {
    boolean useLocalCache = true;
    resetDirectory(installsDirectory);
    if(useLocalCache) {
      Path userCache = realProvisioRoot.resolve(".bin").resolve(".cache");
      provisio = new Provisio(
          userCache,
          installsDirectory,
          profilesDirectory,
          realProvisioRoot.resolve("tool"),
          realProvisioRoot.resolve("profiles"),
          "jvanzyl");
    } else {
      provisio = new Provisio(provisioRoot, "jvanzyl");
    }
  }

  protected Path provisioRoot() {
    return Paths.get(System.getProperty("user.home"), ".provisio");
  }

  protected Path test() {
    return Paths.get("src").resolve("test");
  }

  protected Path target(String name) throws IOException {
    Path path = Paths.get("target").resolve(name);
    Files.createDirectories(path.getParent());
    return path;
  }


}
