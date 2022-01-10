package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.interpolateToolPath;
import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static com.pivovarit.function.ThrowingFunction.unchecked;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.exists;

import ca.vanzyl.provisio.tools.model.ImmutableToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ImmutableToolProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import ca.vanzyl.provisio.tools.util.DownloadManager;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import com.pivovarit.function.ThrowingFunction;
import io.tesla.proviso.archive.UnArchiver;
import io.tesla.proviso.archive.UnArchiver.UnArchiverBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.motd.maven.os.Detector;

public class Provisio {

  // These 4 probably belong somewhere else
  public static final String OS = Detector.normalizeOs(System.getProperty("os.name"));
  public static final String ARCH = Detector.normalizeArch(System.getProperty("os.arch"));
  public static final Path PROVISIO_ROOT = Paths.get(System.getProperty("user.home"), ".provisio");
  public final static Path toolDescriptorDirectory = PROVISIO_ROOT.resolve("tools");
  public final static Path userProfilesDirectory = PROVISIO_ROOT.resolve("profiles");
  public final static Path cache = PROVISIO_ROOT.resolve(".bin").resolve(".cache");
  public final static Path bin = PROVISIO_ROOT.resolve(".bin");

  private final DownloadManager downloadManager;
  private final Map<String, ToolDescriptor> toolDescriptorMap;
  private final YamlMapper<ToolProfile> profileMapper;
  private final YamlMapper<ToolDescriptor> toolMapper;
  private final Path cacheDirectory;
  private final Path installsDirectory;
  private final Path profilesDirectory;
  private final String userProfile;

  public Provisio(String userProfile) throws Exception {
    this(PROVISIO_ROOT, userProfile);
  }

  public Provisio(Path provisioRoot, String userProfile) throws Exception {
    this(
        provisioRoot.resolve("bin").resolve("cache"),
        provisioRoot.resolve("bin").resolve("installs"),
        provisioRoot.resolve("bin").resolve("profiles"),
        userProfile);
  }

  public Provisio(Path cacheDirectory, Path installsDirectory, Path profilesDirectory, String userProfile) throws Exception {
    this.downloadManager = new DownloadManager(cacheDirectory);
    this.toolDescriptorMap = collectToolDescriptorsMap();
    this.profileMapper = new YamlMapper<>();
    this.toolMapper = new YamlMapper<>();
    this.installsDirectory = installsDirectory;
    this.cacheDirectory = cacheDirectory;
    this.profilesDirectory = profilesDirectory;
    this.userProfile = userProfile;
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Tool provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public Path userProfileDirectory() {
    return profilesDirectory.resolve(userProfile);
  }

  public Path cacheDirectory() {
    return cacheDirectory;
  }

  public ToolDescriptor tool(String tool) {
    return toolDescriptorMap.get(tool);
  }

  public ToolProvisioningResult provisionTool(String tool) throws Exception {
    return provisionTool(tool, null);
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Tool provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public ToolProvisioningResult provisionTool(String tool, String version) throws Exception {
    ToolDescriptor toolDescriptor = toolDescriptorMap.get(tool);
    return provisionTool(toolDescriptor, version != null ? version : toolDescriptor.defaultVersion());
  }

  public ToolProvisioningResult provisionTool(ToolDescriptor toolDescriptor, String version) throws Exception {
    ImmutableToolProvisioningResult.Builder result = ImmutableToolProvisioningResult.builder();
    Path installation = installsDirectory.resolve(toolDescriptor.id()).resolve(version);
    Path executable = installation.resolve(toolDescriptor.executable());
    if (exists(installation)) {
      return ImmutableToolProvisioningResult.builder().installation(installation).build();
    }
    Path artifact = downloadManager.resolve(toolDescriptor, version);
    Packaging packaging = toolDescriptor.packaging();
    if (packaging.equals(Packaging.TARGZ) || packaging.equals(Packaging.TARGZ_STRIP) || packaging.equals(Packaging.ZIP) || packaging.equals(Packaging.ZIP_JUNK)) {
      boolean useRoot = !packaging.equals(Packaging.TARGZ_STRIP);
      boolean flatten = packaging.equals(Packaging.ZIP_JUNK);
      UnArchiverBuilder unArchiverBuilder = UnArchiver.builder().useRoot(useRoot).flatten(flatten);
      UnArchiver unArchiver = unArchiverBuilder.build();
      unArchiver.unarchive(artifact.toFile(), installation.toFile());
    } else {
      createDirectories(installation);
      copy(artifact, executable, StandardCopyOption.REPLACE_EXISTING);
      executable.toFile().setExecutable(true);
    }
    // The symllinking might possibly only be for installing not provisioning
    // Create instructions for symlinks and path entries
    // TODO: this needs to be cleaned up as we really only have an installation and it is a single file or dir with
    //  paths to export and generally we should just make it polymorphic
    if (toolDescriptor.layout().equals("file")) {
      Path link = profilesDirectory.resolve(userProfile).resolve(toolDescriptor.executable());
      Path target;
      if (toolDescriptor.tarSingleFileToExtract() != null) {
        String path = interpolateToolPath(toolDescriptor.tarSingleFileToExtract(), toolDescriptor, version);
        target = installation.resolve(path).toAbsolutePath();
      } else {
        target = executable.toAbsolutePath();
      }
      createDirectories(link.getParent());
      if (!exists(link)) {
        createSymbolicLink(link, target);
      }
    } else if (toolDescriptor.layout().equals("directory")) {
      // We want the path relative to the user profile binary directory
      result.addPaths(installsDirectory.relativize(installation.resolve(toolDescriptor.paths())));
    }
    return result.installation(installation).build();
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Profile provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public ToolProfileProvisioningResult provisionProfile() throws Exception {
    return provisionProfile(profileMapper.read(userProfilesDirectory.resolve(userProfile).resolve("profile.yaml"), ToolProfile.class));
  }

  public ToolProfileProvisioningResult provisionProfile(Path profile) throws Exception {
    return provisionProfile(profileMapper.read(profile, ToolProfile.class));
  }

  public ToolProfileProvisioningResult provisionProfile(ToolProfile profile) throws Exception {
    Path initBash = profilesDirectory.resolve(userProfile).resolve(".init.bash");
    touch(initBash);
    line(initBash,"export PROVISIO_ROOT=${HOME}/.provisio%n");
    line(initBash,"export PROVISIO_BIN=${PROVISIO_ROOT}%n");
    line(initBash,"export PROVISIO_INSTALLS=${PROVISIO_ROOT}/bin/installs%n");
    line(initBash,"export PROVISIO_PROFILES=${PROVISIO_ROOT}/bin/profiles%n");
    line(initBash,"export PROVISIO_ACTIVE_PROFILE=${PROVISIO_ROOT}/bin/profiles/profile%n");
    line(initBash,"export PATH=${PROVISIO_BIN}:${PROVISIO_ACTIVE_PROFILE}:${PATH}%n%n");

    ImmutableToolProfileProvisioningResult.Builder profileProvisioningResult = ImmutableToolProfileProvisioningResult.builder();
    for (ToolProfileEntry entry : profile.tools().values()) {
      System.out.println(entry);
      for (String version : entry.version().split("[\\s,]+")) {
        ToolDescriptor tool = toolDescriptorMap.get(entry.name());
        ToolProvisioningResult result = provisionTool(tool, version);
        if(tool.layout().equals("directory") && entry.pathManagedBy() == null) {
          String pathToExport = result.paths().get(0).toString();
          line(initBash,"export PATH=${PROVISIO_INSTALLS}/%s:${PATH}%n", pathToExport);
        }
        profileProvisioningResult.addTools(result);
      }
    }

    Path link = profilesDirectory.resolve("profile");
    Path target = profilesDirectory.resolve(userProfile).toAbsolutePath();
    if(!exists(link)) {
      createSymbolicLink(link, target);
    }
    touch(profilesDirectory.resolve("current"), userProfile);

    return profileProvisioningResult.build();
  }

  private void line(Path path, String line, Object... options) throws IOException {
    Files.writeString(path, String.format(line, options), StandardOpenOption.APPEND);
  }

  private void touch(Path path) throws IOException {
    createDirectories(path.getParent());
    Files.createFile(path);
  }

  private void touch(Path path, String content) throws IOException {
    createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public final static ThrowingFunction<Path, ToolDescriptor, IOException> toolDescriptorFrom =
      path -> new YamlMapper<ToolDescriptor>().read(path, ToolDescriptor.class);

  public final static ThrowingFunction<Path, ToolProfile, IOException> profileDescriptorFrom =
      path -> new YamlMapper<ToolProfile>().read(path, ToolProfile.class);

  public static Map<String, ToolDescriptor> collectToolDescriptorsMap() throws Exception {
    try (Stream<Path> stream = Files.walk(toolDescriptorDirectory, 3)) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(), (i, j) -> j, TreeMap::new));
    }
  }
}
