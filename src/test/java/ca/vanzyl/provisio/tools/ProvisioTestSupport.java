package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.*;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;

import ca.vanzyl.provisio.tools.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioTestSupport {

  protected Provisio provisio;
  protected Path realProvisioRoot = Paths.get(System.getProperty("user.home"), ".provisio");
  protected Path provisioRoot = Paths.get("target", "provisio").toAbsolutePath();
  protected Path cacheDirectory = provisioRoot.resolve("bin").resolve("cache");
  protected Path installsDirectory = provisioRoot.resolve("bin").resolve("installs");
  protected Path profilesDirectory = provisioRoot.resolve("bin").resolve("profiles");

  @Before
  public void setUp() throws Exception {
    boolean useLocalCache = false;
    resetDirectory(installsDirectory);
    if(useLocalCache) {
      Path userCache = realProvisioRoot.resolve("bin").resolve("cache");
      provisio = new Provisio(
          provisioRoot,
          userCache,
          installsDirectory,
          profilesDirectory,
          realProvisioRoot.resolve("tools"),
          realProvisioRoot.resolve("profiles"),
          "jvanzyl");
    } else {
      provisio = new Provisio("jvanzyl");
      //provisio = new Provisio(provisioRoot, "jvanzyl");
    }
  }

  protected Path test() {
    return Paths.get("src").resolve("test");
  }

  protected Path target(String name) throws IOException {
    Path path = Paths.get("target").resolve(name);
    Files.createDirectories(path.getParent());
    return path;
  }

  protected Path touch(String name) throws IOException {
    Path path = Paths.get("target").resolve(name);
    Files.createDirectories(path.getParent());
    Files.writeString(path, name);
    return path;
  }
}
