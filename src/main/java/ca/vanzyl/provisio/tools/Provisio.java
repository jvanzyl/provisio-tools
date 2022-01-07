package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolUrlBuilder.interpolateToolPath;
import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static com.pivovarit.function.ThrowingFunction.unchecked;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.move;

import ca.vanzyl.provisio.tools.model.ImmutableToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ImmutableToolProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import com.pivovarit.function.ThrowingFunction;
import io.tesla.proviso.archive.UnArchiver;
import io.tesla.proviso.archive.UnArchiver.UnArchiverBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
  public final static Path profilesDirectory = PROVISIO_ROOT.resolve("profiles");
  public final static Path cache = PROVISIO_ROOT.resolve(".bin").resolve(".cache");

  private final DownloadManager downloadManager;
  private final Map<String, ToolDescriptor> toolDescriptorMap;
  private final YamlMapper<ToolProfile> profileMapper;
  private final YamlMapper<ToolDescriptor> toolMapper;
  private final Path binaryDirectory;
  private final Path cacheDirectory;

  public Provisio(Path cacheDirectory, Path binaryDirectory) throws Exception {
    this.downloadManager = new DownloadManager(cacheDirectory);
    this.toolDescriptorMap = collectToolDescriptorsMap();
    this.profileMapper = new YamlMapper<>();
    this.toolMapper = new YamlMapper<>();
    this.binaryDirectory = binaryDirectory;
    this.cacheDirectory = cacheDirectory;
  }

  // TARGZ
  // TARGZ_STRIP
  // ZIP
  // ZIP_JUNK
  // FILE
  // INSTALLER, relies on a script but maybe we repackage

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Tool provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public Path cacheDirectory() {
    return cacheDirectory;
  }

  public ToolDescriptor tool(String tool) {
    return toolDescriptorMap.get(tool);
  }

  public ToolProvisioningResult provisionTool(String tool) throws Exception {
    return provisionTool(tool, null);
  }

  public ToolProvisioningResult provisionTool(String tool, String version) throws Exception {
    ToolDescriptor toolDescriptor = toolDescriptorMap.get(tool);
    return provisionTool(toolDescriptor, version != null ? version : toolDescriptor.defaultVersion());
  }

  public ToolProvisioningResult provisionTool(ToolDescriptor toolDescriptor, String version) throws Exception {
    ImmutableToolProvisioningResult.Builder result = ImmutableToolProvisioningResult.builder();
    Path executable = binaryDirectory.resolve(toolDescriptor.executable());
    if (Files.exists(executable)) {
      return ImmutableToolProvisioningResult.builder().executable(executable).build();
    }
    Path artifact = downloadManager.resolve(toolDescriptor, version);
    Packaging packaging = toolDescriptor.packaging();
    if (packaging.equals(Packaging.TARGZ) || packaging.equals(Packaging.TARGZ_STRIP) || packaging.equals(Packaging.ZIP) || packaging.equals(Packaging.ZIP_JUNK)) {
      boolean useRoot = !packaging.equals(Packaging.TARGZ_STRIP);
      boolean flatten = packaging.equals(Packaging.ZIP_JUNK);
      UnArchiverBuilder unArchiverBuilder = UnArchiver.builder()
          .useRoot(useRoot)
          .flatten(flatten);

      // TODO: make an unarchiver processor to rename tarSingleFileToExtract files to the executable
      if(toolDescriptor.tarSingleFileToExtract() != null) {
        String tarSingleFileToExtract = interpolateToolPath(toolDescriptor.tarSingleFileToExtract(), toolDescriptor, version);
        unArchiverBuilder.includes(tarSingleFileToExtract);
      }

      UnArchiver unArchiver = unArchiverBuilder.build();

      if(toolDescriptor.layout().equals("file")) {
        unArchiver.unarchive(artifact.toFile(), binaryDirectory.toFile());
        // Single binaries are placed directly in our binary directory
        if (toolDescriptor.tarSingleFileToExtract() != null) {
          String tarSingleFileToExtract = interpolateToolPath(toolDescriptor.tarSingleFileToExtract(), toolDescriptor, version);
          Path original = binaryDirectory.resolve(tarSingleFileToExtract);
          move(original, executable, StandardCopyOption.REPLACE_EXISTING);
          executable.toFile().setExecutable(true);
        }
        result.executable(executable);
      } else if(toolDescriptor.layout().equals("directory")){
        // An installation is placed in a directory of the form {tool}/{version}/{tree}:
        //
        // jbang
        // └── 0.79.0
        //     ├── jbang
        //     ├── jbang.cmd
        //     ├── jbang.jar
        //     └── jbang.ps1
        //
        Path installationDirectory = binaryDirectory.resolve(toolDescriptor.id()).resolve(version);
        unArchiver.unarchive(artifact.toFile(), installationDirectory.toFile());
        result.installation(installationDirectory);
      }
    } else {
      // Copy the single file over and make executable
      copy(artifact, executable, StandardCopyOption.REPLACE_EXISTING);
      executable.toFile().setExecutable(true);
      result.executable(executable);
    }
    return result.build();
  }

  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Profile provisioning
  // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  public ToolProfileProvisioningResult provisionProfile(String profile) throws Exception {
    return provisionProfile(profileMapper.read(profilesDirectory.resolve(profile).resolve("profile.yaml"), ToolProfile.class));
  }

  public ToolProfileProvisioningResult provisionProfile(Path profile) throws Exception {
    return provisionProfile(profileMapper.read(profile, ToolProfile.class));
  }

  public ToolProfileProvisioningResult provisionProfile(ToolProfile profile) throws Exception {
    ImmutableToolProfileProvisioningResult.Builder result = ImmutableToolProfileProvisioningResult.builder();
    for(ToolProfileEntry entry : profile.tools().values()) {
      System.out.println(entry);
      ToolProvisioningResult toolProvisioningResult = provisionTool(entry.name());
      result.addTools(toolProvisioningResult);
    }
    return result.build();
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
