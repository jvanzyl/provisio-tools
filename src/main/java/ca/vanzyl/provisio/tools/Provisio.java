package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.interpolateToolPath;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.mapArch;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.mapOs;
import static com.pivovarit.function.ThrowingFunction.unchecked;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;

import ca.vanzyl.provisio.archive.UnArchiver;
import ca.vanzyl.provisio.archive.UnArchiver.UnArchiverBuilder;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ImmutableToolProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import ca.vanzyl.provisio.tools.util.DownloadManager;
import ca.vanzyl.provisio.tools.util.PostInstall;
import ca.vanzyl.provisio.tools.util.ShellFileModifier;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import com.pivovarit.function.ThrowingFunction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.motd.maven.os.Detector;

public class Provisio {

  public static final String POST_INSTALL = "post-install.sh";
  public static final String TOOL_DESCRIPTOR = "descriptor.yml";
  public static final String SHELL_TEMPLATE = "bash-template.txt";

  public static final String OS = Detector.normalizeOs(System.getProperty("os.name"));
  public static final String ARCH = Detector.normalizeArch(System.getProperty("os.arch"));

  private final DownloadManager downloadManager;
  private final Map<String, ToolDescriptor> toolDescriptorMap;
  private final YamlMapper<ToolProfile> profileMapper;
  private final Path provisioRoot;
  // ${HOME}/.provisio/bin/{cache|installs|profiles}
  private final Path cacheDirectory;
  private final Path installsDirectory;
  private final Path profilesDirectory;
  private final Path binaryProfileDirectory;
  // ${HOME}/.provisio/{tools|profiles}
  public final Path toolDescriptorDirectory;
  public final Path userProfilesDirectory;
  private final String userProfile;

  private final Path userHome;

  // Current profile.yaml file that lists all the tools
  private final Path userProfileYaml;

  public Provisio(String userProfile) throws Exception {
    this(Paths.get(System.getProperty("user.home"), ".provisio"), userProfile);
  }

  public Provisio(Path provisioRoot, String userProfile) throws Exception {
    this(
        provisioRoot,
        provisioRoot.resolve("bin").resolve("cache"),
        provisioRoot.resolve("bin").resolve("installs"),
        provisioRoot.resolve("bin").resolve("profiles"),
        provisioRoot.resolve("tools"),
        provisioRoot.resolve("profiles"),
        userProfile);
  }

  public Provisio(
      Path provisioRoot,
      Path cacheDirectory,
      Path installsDirectory,
      Path profilesDirectory,
      Path toolDescriptorDirectory,
      Path userProfilesDirectory,
      String userProfile) throws Exception {
    this.downloadManager = new DownloadManager(cacheDirectory);
    this.profileMapper = new YamlMapper<>();

    this.provisioRoot = provisioRoot;
    this.installsDirectory = installsDirectory;
    this.cacheDirectory = cacheDirectory;
    this.toolDescriptorDirectory = toolDescriptorDirectory;
    //
    this.userProfile = userProfile; // name of the profile
    this.profilesDirectory = profilesDirectory;
    this.binaryProfileDirectory = profilesDirectory.resolve(userProfile);
    // config
    this.userProfileYaml = userProfilesDirectory.resolve(userProfile).resolve("profile.yaml");
    this.userProfilesDirectory = userProfilesDirectory;
    this.userHome = Paths.get(System.getProperty("user.home"));

    initialize();
    this.toolDescriptorMap = collectToolDescriptorsMap();
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Initialization
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public void initialize() throws Exception {
    // This reflection code doesn't work in Graal
    System.out.println("Initializing provisio");
    String prefix = "provisioRoot";
    int index = prefix.length();
    try(InputStream resourceDescriptorInput = Provisio.class.getClassLoader().getResource("provisioRoot/resources").openStream()) {
      List<String> resources = new BufferedReader(new InputStreamReader(resourceDescriptorInput, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
      for (String resource : resources) {
        Path path = provisioRoot.resolve(resource);
        createDirectories(path.getParent());
        try (InputStream is = Provisio.class.getClassLoader().getResource("provisioRoot/" + resource).openStream();
            OutputStream os = Files.newOutputStream(path)) {
          is.transferTo(os);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Tool provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO: remove this and build it into testing
  public Path userProfileDirectory() {
    return profilesDirectory.resolve(userProfile);
  }

  // TODO: remove this and build it into testing
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
    Path toolInstallation = installsDirectory.resolve(toolDescriptor.id()).resolve(version);
    Path executable = toolInstallation.resolve(toolDescriptor.executable());

    ImmutableToolProvisioningResult.Builder result = ImmutableToolProvisioningResult.builder();
    // We want the path relative to the user profile binary directory
    result.addPaths(installsDirectory.relativize(toolInstallation.resolve(toolDescriptor.paths())));
    result.installation(toolInstallation);

    if (!exists(toolInstallation)) {
      Path artifact = downloadManager.resolve(toolDescriptor, version);
      Packaging packaging = toolDescriptor.packaging();
      if (packaging.equals(Packaging.TARGZ) || packaging.equals(Packaging.TARGZ_STRIP) || packaging.equals(Packaging.ZIP) || packaging.equals(Packaging.ZIP_JUNK)) {
        boolean useRoot = !packaging.equals(Packaging.TARGZ_STRIP);
        boolean flatten = packaging.equals(Packaging.ZIP_JUNK);
        UnArchiverBuilder unArchiverBuilder = UnArchiver.builder().useRoot(useRoot).flatten(flatten);
        UnArchiver unArchiver = unArchiverBuilder.build();
        unArchiver.unarchive(artifact.toFile(), toolInstallation.toFile());
      } else {
        createDirectories(toolInstallation);
        copy(artifact, executable, StandardCopyOption.REPLACE_EXISTING);
        executable.toFile().setExecutable(true);
      }
    }

    // The symllinking might possibly only be for installing not provisioning
    // Create instructions for symlinks and path entries
    // TODO: this needs to be cleaned up as we really only have an installation and it is a single file or dir with
    //  paths to export and generally we should just make it polymorphic
    if (toolDescriptor.layout().equals("file")) {
      Path link = binaryProfileDirectory.resolve(toolDescriptor.executable());
      Path target;
      if (toolDescriptor.tarSingleFileToExtract() != null) {
        String path = interpolateToolPath(requireNonNull(toolDescriptor.tarSingleFileToExtract()), toolDescriptor, version);
        target = toolInstallation.resolve(path).toAbsolutePath();
      } else {
        target = executable.toAbsolutePath();
      }
      createDirectories(link.getParent());
      if (!exists(link)) {
        //   target = ${provisioRoot}/bin/installs/argocd/2.1.7/argocd
        //     link = ${provisioRoot}/bin/profiles/jvanzyl/argocd
        // relative = ../../installs/argocd/2.1.7/argocd
        createSymbolicLink(link, link.getParent().relativize(target));
      }
    }
    return result.build();
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
    String provisioRootRelativeToUserHome = userHome.relativize(provisioRoot).toString();

    Path initBash = binaryProfileDirectory.resolve(".init.bash");
    touch(initBash);
    line(initBash, "export PROVISIO_ROOT=${HOME}/%s%n", provisioRootRelativeToUserHome);
    line(initBash, "export PROVISIO_BIN=${PROVISIO_ROOT}%n");
    line(initBash, "export PROVISIO_INSTALLS=${PROVISIO_ROOT}/bin/installs%n");
    line(initBash, "export PROVISIO_PROFILES=${PROVISIO_ROOT}/bin/profiles%n");
    line(initBash, "export PROVISIO_ACTIVE_PROFILE=${PROVISIO_ROOT}/bin/profiles/profile%n");
    line(initBash, "export PATH=${PROVISIO_BIN}:${PROVISIO_ACTIVE_PROFILE}:${PATH}%n%n");

    ImmutableToolProfileProvisioningResult.Builder profileProvisioningResult = ImmutableToolProfileProvisioningResult.builder();
    for (ToolProfileEntry entry : profile.tools().values()) {
      System.out.println(entry);
      ToolDescriptor tool = toolDescriptorMap.get(entry.name());
      Path toolDirectory = toolDescriptorDirectory.resolve(tool.id());
      for (String version : entry.version().split("[\\s,]+")) {
        ToolProvisioningResult result = provisionTool(tool, version);

        // TODO: should the policy be these scripts be idempotent? yes
        // This needs to be more testable.
        Path postInstallScript = toolDirectory.resolve(POST_INSTALL);
        if (exists(postInstallScript)) {
          List<String> args = List.of(
              postInstallScript.toAbsolutePath().toString(),
              // ${1}
              provisioRoot.resolve("libexec").resolve("provisio-functions.bash").toAbsolutePath().toString(),
              // ${2}
              userProfileYaml.toAbsolutePath().toString(),
              // ${3}
              binaryProfileDirectory.toString(),
              //result.executable() != null ?result.executable().toAbsolutePath().toString() : "executable",
              // ${4}
              "filename", // appear not to be used but hold place
              // ${5}
              "url", // ditto
              // ${6}
              version,
              // ${7}
              tool.id(),
              // ${8}
              result.installation() != null ? result.installation().toAbsolutePath().toString() : "location",
              // ${9} : this is the straight version not mapped from descriptor
              mapOs(OS, tool),
              // ${10} : this is the straight version not mapped from descriptor
              mapArch(ARCH, tool),
              // ${11}
              installsDirectory.toAbsolutePath().toString(),
              // ${12}: relative installation directroy from binary profile directory
              result.installation() != null ? binaryProfileDirectory.relativize(result.installation()).toString() : "relative"
          );
          PostInstall postInstall = new PostInstall(toolDirectory, args);
          postInstall.execute();
        }

        // These are installations where the path needs to be added to the environment
        if (tool.layout().equals("directory") && entry.pathManagedBy() == null) {
          // Shell template additions
          Path shellTemplate = toolDirectory.resolve(SHELL_TEMPLATE);
          line(initBash, "# -------------- " + tool.id() + "  --------------%n");
          if (exists(shellTemplate)) {
            String shellTemplateContents = interpolateToolPath(Files.readString(shellTemplate), tool, version);
            line(initBash, shellTemplateContents + "%n");
          } else {
            String pathToExport = result.paths().get(0).toString();
            String toolRoot = tool.id().replace("-", "_").toUpperCase() + "_ROOT";
            line(initBash, toolRoot + "=${PROVISIO_INSTALLS}/%s%n", pathToExport);
            line(initBash, "export PATH=${%s}:${PATH}%n%n", toolRoot);
          }
        }
        profileProvisioningResult.addTools(result);
      }
    }

    Path link = profilesDirectory.resolve("profile");
    Path target = profilesDirectory.resolve(userProfile).toAbsolutePath();
    if (!exists(link)) {
      createSymbolicLink(link, target);
    }
    touch(profilesDirectory.resolve("current"), userProfile);

    // Shell init file update
    ShellFileModifier modifier = new ShellFileModifier(userHome, provisioRoot);
    modifier.updateShellInitializationFile();

    return profileProvisioningResult.build();
  }

  private void line(Path path, String line, Object... options) throws IOException {
    Files.writeString(path, String.format(line, options), StandardOpenOption.APPEND);
  }

  private void touch(Path path) throws IOException {
    createDirectories(path.getParent());
    // Without this line it fails in Graal, some some default modes must be different
    deleteIfExists(path);
    createFile(path);
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
    Path tools = Paths.get(System.getProperty("user.home"), ".provisio").resolve("tools");
    try (Stream<Path> stream = Files.walk(tools, 3)) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(), (i, j) -> j, TreeMap::new));
    }
  }
}