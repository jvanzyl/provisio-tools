package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.copyFolder;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;

import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest.Builder;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.tool.ToolMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class ProvisioTestSupport {

  protected ProvisioningRequest request;
  protected Provisio provisio;
  protected Path realProvisioRoot = get(System.getProperty("user.home"), ".provisio");
  protected Path testProvisioRoot = get("target", ".provisio").toAbsolutePath();
  protected Path dotProvisio = get("src/test/.provisio").toAbsolutePath();
  protected String userProfile;
  protected ToolMapper toolMapper;

  @Before
  public void setUp() throws Exception {
    userProfile = "provisio";
    // On GHA runners this setting doesn't matter until we figure out some caching
    boolean useLocalCache = true;
    // Having this be set to true is for provisio developers experimenting locally
    boolean useRealProvisioRoot = false;
    Builder builder = ImmutableProvisioningRequest.builder();
    builder.userProfile(userProfile);
    builder.provisioRoot(useRealProvisioRoot ? realProvisioRoot : testProvisioRoot);
    if (!useRealProvisioRoot && useLocalCache) {
      builder.cacheDirectory(realProvisioRoot.resolve("bin").resolve("cache"));
    }
    copyFolder(dotProvisio, testProvisioRoot);
    request = builder.build();
    provisio = new Provisio(builder.build());
    toolMapper = new ToolMapper();
  }

  protected Path userBinaryProfileDirectory() {
    return request.binaryProfilesDirectory().resolve(userProfile);
  }

  public static Provisio provisio(Path provisioRoot, String userProfile) throws Exception {
    Builder builder = ImmutableProvisioningRequest.builder();
    builder.provisioRoot(provisioRoot);
    builder.userProfile(userProfile);
    return new Provisio(builder.build());
  }

  protected Path testProfile(String name) {
    return get("src/test/profiles").resolve(name).resolve("profile.yaml");
  }

  public static Path directory(String name) throws IOException {
    Path path = get("target").resolve(name).toAbsolutePath();
    createDirectories(path);
    return path;
  }

  public static Path path(String name) throws IOException {
    Path path = get("target").resolve(name).toAbsolutePath();
    createDirectories(path.getParent());
    return path;
  }

  public static Path path(Path directory, String name) throws IOException {
    Path path = directory.resolve(name).toAbsolutePath();
    createDirectories(path.getParent());
    return path;
  }

  public static Path touch(Path directory, String name) throws IOException {
    Path path = directory.resolve(name);
    createDirectories(directory);
    writeString(path, name);
    return path;
  }

  public ToolDescriptor toolDescriptor(String toolId) throws IOException {
    Path path = get("src/main/resources/provisioRoot/config/tools").resolve(toolId).resolve(ToolDescriptor.DESCRIPTOR);
    return toolMapper.toolDescriptor(path);
  }

}
