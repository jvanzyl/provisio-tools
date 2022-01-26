package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.*;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;

import ca.vanzyl.provisio.tools.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioTestSupport {

  protected Provisio provisio;
  protected Path realProvisioRoot = get(System.getProperty("user.home"), ".provisio");
  protected Path provisioRoot = get("target", ".provisio").toAbsolutePath();
  protected Path cacheDirectory = provisioRoot.resolve("bin").resolve("cache");
  protected Path installsDirectory = provisioRoot.resolve("bin").resolve("installs");
  protected Path profilesDirectory = provisioRoot.resolve("bin").resolve("profiles");
  protected String userProfile;

  @Before
  public void setUp() throws Exception {
    userProfile = "jvanzyl";
    boolean useLocalCache = false;
    //resetDirectory(installsDirectory);
    if(useLocalCache) {
      Path userCache = realProvisioRoot.resolve("bin").resolve("cache");
      provisio = new Provisio(
          provisioRoot,
          userCache,
          installsDirectory,
          profilesDirectory,
          realProvisioRoot.resolve("tools"),
          realProvisioRoot.resolve("profiles"),
          userProfile);
    } else {
      provisio = new Provisio(userProfile);
      //provisio = new Provisio(provisioRoot, "jvanzyl");
    }
  }

  protected Path userProfileDirectory() {
    return profilesDirectory.resolve(userProfile);
  }

  protected Path test() {
    return get("src").resolve("test");
  }

  protected Path path(String name) throws IOException {
    Path path = get("target").resolve(name).toAbsolutePath();
    createDirectories(path.getParent());
    return path;
  }

  protected Path path(Path directory, String name) throws IOException {
    Path path = directory.resolve(name).toAbsolutePath();
    createDirectories(path.getParent());
    return path;
  }


  protected Path touch(Path directory, String name) throws IOException {
    Path path = directory.resolve(name);
    createDirectories(directory);
    writeString(path, name);
    return path;
  }

  protected Path touch(String name) throws IOException {
    Path path = get("target").resolve(name);
    createDirectories(path.getParent());
    writeString(path, name);
    return path;
  }
}
