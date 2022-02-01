package ca.vanzyl.provisio.tools;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;

import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest.Builder;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioTestSupport {

  protected ProvisioningRequest request;
  protected Provisio provisio;
  protected Path realProvisioRoot = get(System.getProperty("user.home"), ".provisio");
  protected Path testProvisioRoot = get("target", ".provisio").toAbsolutePath();
  protected String userProfile;

  @Before
  public void setUp() throws Exception {
    userProfile = "jvanzyl";
    boolean useLocalCache = false;
    boolean useRealProvisioRoot = true;
    Builder builder = ImmutableProvisioningRequest.builder();
    builder.provisioRoot(useRealProvisioRoot ? realProvisioRoot : testProvisioRoot);
    if (!useRealProvisioRoot && useLocalCache) {
      builder.cacheDirectory(realProvisioRoot.resolve("bin").resolve("cache"));
    }
    request = builder.build();
    provisio = new Provisio(builder.build(), userProfile);
  }

  protected Path userBinaryProfileDirectory() {
    return request.binaryProfilesDirectory().resolve(userProfile);
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
}
