package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static ca.vanzyl.provisio.tools.shell.ShellHandler.Shell.FISH;
import static ca.vanzyl.provisio.tools.shell.ShellHandler.Shell.ZSH;
import static ca.vanzyl.provisio.tools.shell.ShellHandler.userShell;
import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static ca.vanzyl.provisio.tools.util.FileUtils.makeExecutable;
import static ca.vanzyl.provisio.tools.util.FileUtils.moveDirectoryIfExists;
import static ca.vanzyl.provisio.tools.util.FileUtils.touch;
import static ca.vanzyl.provisio.tools.util.FileUtils.updateRelativeSymlink;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.interpolateToolPath;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.mapArch;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.mapOs;
import static com.pivovarit.function.ThrowingFunction.unchecked;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isExecutable;
import static java.nio.file.Files.move;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;

import ca.vanzyl.provisio.archive.UnArchiver;
import ca.vanzyl.provisio.archive.UnArchiver.UnArchiverBuilder;
import ca.vanzyl.provisio.tools.generator.github.GitHubLatestReleaseFinder;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ImmutableToolProvisioningResult;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import ca.vanzyl.provisio.tools.shell.BashShellHandler;
import ca.vanzyl.provisio.tools.shell.FishShellHandler;
import ca.vanzyl.provisio.tools.shell.ShellHandler;
import ca.vanzyl.provisio.tools.shell.ZshShellHandler;
import ca.vanzyl.provisio.tools.util.CliCommand;
import ca.vanzyl.provisio.tools.util.PostInstall;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import ca.vanzyl.provisio.tools.util.http.DownloadManager;
import com.pivovarit.function.ThrowingFunction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.motd.maven.os.Detector;

public class Provisio {

  // de-dupe these
  public static final String PROVISiO_SHELL_INIT = ".init.bash";
  public static final String POST_INSTALL = "post-install.sh";
  public final static String IN_PROGRESS_EXTENSION = ".in-progress";
  public final static String PROFILE_YAML = "profile.yaml";
  public final static String PROFILE_SHELL = "profile.shell";
  //public final static String PROVISIO_RELEASES_URL = "https://github.com/jvanzyl/provisio-binaries/releases";
  public final static String PROVISIO_RELEASES_URL = "https://github.com/jvanzyl/provisio-tools/releases";

  public static final String OS = Detector.normalizeOs(System.getProperty("os.name"));
  public static final String ARCH = Detector.normalizeArch(System.getProperty("os.arch"));

  private final DownloadManager downloadManager;
  private final Map<String, ToolDescriptor> toolDescriptorMap;
  private final YamlMapper<ToolProfile> profileMapper;
  // ${HOME}/.provisio/bin/{cache|installs|profiles}
  private final Path cacheDirectory;
  private final Path installsDirectory;
  private final Path binaryProfilesDirectory;
  private final Path binaryProfileDirectory;
  // ${HOME}/.provisio/{tools|profiles}
  public final Path toolDescriptorDirectory;

  // This is context specific
  // Current profile.yaml file that lists all the tools
  private final Path userHome;
  private final String userProfile;
  private final Path dotProvisioUserProfileYaml;
  private final Path workingDirectoryUserProfileYaml;

  private final ProvisioningRequest request;

  public Provisio() throws Exception {
    this(ImmutableProvisioningRequest.builder().build());
  }

  public Provisio(ProvisioningRequest request) throws Exception {
    this.request = request;
    this.installsDirectory = request.installsDirectory();
    this.cacheDirectory = request.cacheDirectory();
    this.binaryProfilesDirectory = request.binaryProfilesDirectory();

    this.toolDescriptorDirectory = request.toolDescriptorsDirectory();
    this.downloadManager = new DownloadManager(cacheDirectory);
    this.profileMapper = new YamlMapper<>();

    // config, really all user profile context
    this.userHome = get(System.getProperty("user.home"));
    //this.userProfile = userProfile != null ? userProfile : findCurrentProfile();
    this.userProfile = request.activeUserProfile();
    this.binaryProfileDirectory = binaryProfilesDirectory.resolve(this.userProfile);
    this.dotProvisioUserProfileYaml = request.userProfilesDirectory().resolve(this.userProfile).resolve(PROFILE_YAML);
    this.workingDirectoryUserProfileYaml = request.workingDirectoryProfilesDirectory().resolve(this.userProfile).resolve(PROFILE_YAML);

    initialize();
    // TODO We probably don't want to read them all in. What happens when there are 10k of these?
    this.toolDescriptorMap = collectToolDescriptorsMap(request.toolDescriptorsDirectory());
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Initialization
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public void initialize() throws Exception {
    Path userProfileYaml = findUserProfileYaml();
    System.out.println("Initializing provisio[profile=" + userProfile + " with " + userProfileYaml + "]");
    //
    // Write out the copy of the resources into new directory and when it is successfully written to disk then we move
    // current directory out of the way and then move the new directory to the current
    //
    // 1) ${provisioRoot}/config   -> ${provisioRoot}/config.lastRevision
    // 2) resources from classpath -> ${provisioRoot}/config.inProgress
    // 3) remove ${provisioRoot}/config.lastRevision: This signals the configuration has successfully from the classpath
    //
    // The resources file currently is of the form:
    //
    // config/tools/dive/descriptor.yml
    // config/tools/kapp/descriptor.yml
    // config/tools/argocd/descriptor.yml
    //

    // 1)
    moveDirectoryIfExists(request.configDirectory(), request.configLastRevisionDirectory());

    // 2)
    try (InputStream resourceDescriptorInput = Provisio.class.getClassLoader().getResource("provisioRoot/resources").openStream()) {
      List<String> resources = new BufferedReader(new InputStreamReader(resourceDescriptorInput, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
      for (String resource : resources) {
        Path path = request.provisioRoot().resolve(resource);
        createDirectories(path.getParent());
        try (InputStream is = Provisio.class.getClassLoader().getResource("provisioRoot/" + resource).openStream();
            OutputStream os = newOutputStream(path)) {
          is.transferTo(os);
        }
      }
    }

    // 3)
    deleteDirectoryIfExists(request.configLastRevisionDirectory());
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  //
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  private Path findUserProfileYaml() {
    if (exists(workingDirectoryUserProfileYaml)) {
      return workingDirectoryUserProfileYaml;
    } else if (exists(dotProvisioUserProfileYaml)) {
      return dotProvisioUserProfileYaml;
    }
    return null;
  }

  private void message(String message, String... formats) {
    System.out.format(message, formats);
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Self update
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public void selfUpdate() throws Exception {
    message("Self updating provisio ...");
    // TODO: make these all constants
    // TODO: don't update if already up to date
    // Fetch the latest release of provisio and replace the main executable with a symlink
    GitHubLatestReleaseFinder finder = new GitHubLatestReleaseFinder();
    String latestProvisioVersion = finder.find(PROVISIO_RELEASES_URL).version();
    ToolProvisioningResult result = provisionTool("provisio", latestProvisioVersion);
    // this is null?
    // Path target = result.executable();
    Path target = result.installation().resolve("provisio");
    Path link = request.provisioRoot().resolve("provisio");
    updateRelativeSymlink(link, target);
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Tool provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO: remove this and build it into testing
  public Path cacheDirectory() {
    return cacheDirectory;
  }

  public ToolDescriptor tool(String tool) {
    return toolDescriptorMap.get(tool);
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

    ImmutableToolProvisioningResult.Builder toolProvisioningResultBuilder = ImmutableToolProvisioningResult.builder();
    toolProvisioningResultBuilder.toolDescriptor(toolDescriptor);
    toolProvisioningResultBuilder.version(version);
    toolProvisioningResultBuilder.installation(toolInstallation);

    if (!exists(toolInstallation)) {
      Path artifact = downloadManager.resolve(toolDescriptor, version);
      Packaging packaging = toolDescriptor.packaging();
      if (packaging.equals(Packaging.TARGZ) ||
          packaging.equals(Packaging.TARGZ_STRIP) ||
          packaging.equals(Packaging.ZIP) ||
          packaging.equals(Packaging.ZIP_JUNK)) {
        boolean useRoot = !packaging.equals(Packaging.TARGZ_STRIP);
        boolean flatten = packaging.equals(Packaging.ZIP_JUNK);
        //
        // When we unarchive artifacts we do so in an in progress directory so that if the unarchiving is interrupted
        // we can remove the partially unarchived artifact and start over.
        //
        Path inProgress = toolInstallation.resolveSibling(toolInstallation.getFileName() + IN_PROGRESS_EXTENSION);
        deleteDirectoryIfExists(inProgress);
        UnArchiverBuilder unArchiverBuilder = UnArchiver.builder().useRoot(useRoot).flatten(flatten);
        UnArchiver unArchiver = unArchiverBuilder.build();
        unArchiver.unarchive(artifact.toFile(), inProgress.toFile());
        move(inProgress, toolInstallation, StandardCopyOption.ATOMIC_MOVE);
      } else {
        // Combine all these in FileUtils
        createDirectories(toolInstallation);
        copy(artifact, executable, REPLACE_EXISTING);
        makeExecutable(executable);
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
      //
      //   target = ${provisioRoot}/bin/installs/argocd/2.1.7/argocd
      //     link = ${provisioRoot}/bin/profiles/jvanzyl/argocd
      // relative = ../../installs/argocd/2.1.7/argocd
      //
      updateRelativeSymlink(link, target);
    }
    return toolProvisioningResultBuilder.build();
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Provision profile
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Install profile
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public ToolProfileProvisioningResult installProfile() throws Exception {
    Path userProfileYaml = findUserProfileYaml();
    if (userProfileYaml != null) {
      return installProfile(userProfileYaml);
    } else {
      // The ${HOME}.provisio/profiles and ${PWD}/.provisio/profiles directories don't contain the requested profile
      String errorMessage = format("The profile %s doesn't exists in: %n%n %s %n%n or %n%n %s %n%n Do you have the right profile name?",
          userProfile, workingDirectoryUserProfileYaml, dotProvisioUserProfileYaml);

      return ImmutableToolProfileProvisioningResult.builder()
          .provisioningSuccessful(false)
          .errorMessage(errorMessage)
          .build();
    }
  }

  // This should be installation result
  public ToolProfileProvisioningResult installProfile(Path profileYaml) throws Exception {
    ToolProfile profile = profileMapper.read(profileYaml, ToolProfile.class);
    Path profileYamlRecord = binaryProfileDirectory.resolve(PROFILE_YAML);
    ToolProfile profileRecord;
    if (exists(profileYamlRecord)) {
      // This profile has been provisioned previously
      profileRecord = profileMapper.read(profileYaml, ToolProfile.class);
    } else {
      profileRecord = profile;
    }

    //
    // Provision
    //
    Path prereqs = request.libexecDirectory().resolve(OS.toLowerCase() + ".bash");
    if (exists(prereqs)) {
      // Install prereqs for the OS by running the OS specific script to bootstrap things. Trying to move core
      // utils to a binary build for not requiring brew at all.
      if (!isExecutable((prereqs))) {
        makeExecutable(prereqs);
      }
      CliCommand command = new CliCommand(List.of(prereqs.toAbsolutePath().toString()), prereqs.getParent(), Map.of(), false);
      CliCommand.Result result = command.execute();
    }

    //
    // Provision
    //
    ImmutableToolProfileProvisioningResult.Builder profileProvisioningResultBuilder = ImmutableToolProfileProvisioningResult.builder();
    for (ToolProfileEntry entry : profile.tools().values()) {
      ToolProfileEntry entryRecord = profileRecord.tools().get(entry.name());
      if (entry.version().equals(entryRecord.version())) {
        System.out.println(entry + " Up to date");
      } else {
        System.out.println(entry + " Updating ...");
      }
      ToolDescriptor tool = toolDescriptorMap.get(entry.name());
      Path toolDirectory = toolDescriptorDirectory.resolve(tool.id());
      for (String version : entry.version().split("[\\s,]+")) {
        ToolProvisioningResult toolProvisioningResult =
            ImmutableToolProvisioningResult.builder().from(provisionTool(tool, version)).pathManagedBy(entry.pathManagedBy()).build();
        Path postInstallScript = toolDirectory.resolve(POST_INSTALL);
        if (exists(postInstallScript)) {
          List<String> args = List.of(
              postInstallScript.toAbsolutePath().toString(),
              // ${1}
              request.libexecDirectory().resolve("provisio-functions.bash").toAbsolutePath().toString(),
              // ${2}
              profileYaml.toAbsolutePath().toString(),
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
              toolProvisioningResult.installation() != null ? toolProvisioningResult.installation().toAbsolutePath().toString() : "location",
              // ${9} : this is the straight version not mapped from descriptor
              mapOs(OS, tool),
              // ${10} : this is the straight version not mapped from descriptor
              mapArch(ARCH, tool),
              // ${11}
              installsDirectory.toAbsolutePath().toString(),
              // ${12}: relative installation directroy from binary profile directory
              toolProvisioningResult.installation() != null ? binaryProfileDirectory.relativize(toolProvisioningResult.installation()).toString() : "relative"
          );
          PostInstall postInstall = new PostInstall(toolDirectory, args);
          postInstall.execute();
        }
        profileProvisioningResultBuilder.addTools(toolProvisioningResult);
      }
    }

    //
    // Install
    //
    ShellHandler shellHandler;
    if (userShell().equals(FISH)) {
      shellHandler = new FishShellHandler(userHome, request);
    } else if (userShell().equals(ZSH)) {
      shellHandler = new ZshShellHandler(userHome, request);
    } else {
      shellHandler = new BashShellHandler(userHome, request);
    }

    shellHandler.preamble();
    String shellTemplateName = shellHandler.shellTemplateName();

    ToolProfileProvisioningResult profileProvisioningResult = profileProvisioningResultBuilder.build();
    for (ToolProvisioningResult toolProvisioningResult : profileProvisioningResultBuilder.build().tools()) {
      String version = toolProvisioningResult.version();
      String pathManagedBy = toolProvisioningResult.pathManagedBy();
      ToolDescriptor tool = toolProvisioningResult.toolDescriptor();
      Path toolDirectory = toolDescriptorDirectory.resolve(tool.id());
      // These are installations where the path needs to be added to the environment
      if (tool.layout().equals("directory") && pathManagedBy == null) {
        Path shellTemplatePath = toolDirectory.resolve(shellTemplateName);
        shellHandler.comment(tool.id());
        if (exists(shellTemplatePath)) {
          String shellTemplateContents = interpolateToolPath(readString(shellTemplatePath), tool, version);
          shellHandler.write(shellTemplateContents);
        } else {
          //
          // Produces something like the following:
          //
          // # -------------- pulumi  --------------
          // PULUMI_ROOT=${PROVISIO_INSTALLS}/pulumi/3.22.1
          // export PATH=${PULUMI_ROOT}:${PATH}
          //
          // And the case where there are multiple paths to export:
          //
          // "." and "bin"
          //
          // # -------------- krew  --------------
          // KREW_ROOT=${PROVISIO_INSTALLS}/krew/0.42.0
          // export PATH=${KREW_ROOT}:${KREW_ROOT}/bin:${PATH}
          // export PATH=${KREW_ROOT}/bin:${PATH}
          //
          Path toolInstallation = installsDirectory.resolve(tool.id()).resolve(version);
          String relativeToolInstallationPath = installsDirectory.relativize(toolInstallation).toString();
          String toolRoot = tool.id().replace("-", "_").toUpperCase() + "_ROOT";
          shellHandler.pathWithExport(toolRoot, relativeToolInstallationPath, tool.paths());
        }
      }
    }

    // If the profile.shell exists then make the addition to the .init.bash
    Path userProfileShell = profileYaml.getParent().resolve(PROFILE_SHELL);
    if (exists(userProfileShell)) {
      String userProfileShellContents = readString(userProfileShell);
      shellHandler.write(userProfileShellContents);
    }

    // Update the symlink to the currently active profile
    Path target = binaryProfilesDirectory.resolve(userProfile).toAbsolutePath();
    Path link = binaryProfilesDirectory.resolve("profile");
    deleteIfExists(link);
    createSymbolicLink(link, binaryProfilesDirectory.relativize(target));
    touch(binaryProfilesDirectory.resolve("current"), userProfile);

    // Shell init file update
    System.out.println();
    Path shellFile = shellHandler.updateShellInitialization();
    System.out.println("Updated: " + shellFile);

    // We record what was installed for the profile
    copy(profileYaml, profileYamlRecord, REPLACE_EXISTING);

    return profileProvisioningResult;
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public final static ThrowingFunction<Path, ToolDescriptor, IOException> toolDescriptorFrom =
      path -> new YamlMapper<ToolDescriptor>().read(path, ToolDescriptor.class);

  public static Map<String, ToolDescriptor> collectToolDescriptorsMap(Path tools) throws Exception {
    try (Stream<Path> stream = walk(tools, 3)) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(), (i, j) -> j, TreeMap::new));
    }
  }
}