package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.model.ToolDescriptor.DESCRIPTOR;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.buildUrlFor;
import static com.pivovarit.function.ThrowingFunction.unchecked;

import ca.vanzyl.provisio.tools.model.ImmutableToolProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import com.pivovarit.function.ThrowingFunction;
import io.tesla.proviso.archive.UnArchiver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
  public static final Path PROVISIO_ROOT = Paths.get(System.getProperty("user.home"), ".provisio/");
  public final static Path toolDescriptorDirectory = PROVISIO_ROOT.resolve("tools");

  private final DownloadManager downloadManager;
  private final Map<String, ToolDescriptor> toolDescriptorMap;

  public Provisio(Path target) throws Exception {
    this.downloadManager = new DownloadManager(target);
    this.toolDescriptorMap = collectToolDescriptorsMap();
  }

  // TARGZ
  // TARGZ_STRIP
  // ZIP
  // FILE
  // GIT, which can just be a tarball: https://stackoverflow.com/questions/8377081/github-api-download-zip-or-tarball-link
  // INSTALLER, relies on a script but maybe we repackage

  public ToolProvisioningResult provisionTool(Path targetDirectory, String tool) throws Exception {
    return provisionTool(targetDirectory, tool, null);
  }

  public ToolProvisioningResult provisionTool(Path targetDirectory, String tool, String version) throws Exception {
    ToolDescriptor toolDescriptor = toolDescriptorMap.get(tool);
    return provisionTool(targetDirectory, toolDescriptor, version != null ? version : toolDescriptor.defaultVersion());
  }

  public ToolProvisioningResult provisionTool(Path targetDirectory, ToolDescriptor toolDescriptor, String version) throws Exception {
    Path executable = targetDirectory.resolve(toolDescriptor.executable());
    if (Files.exists(executable)) {
      return ImmutableToolProvisioningResult.builder().executable(executable).build();
    }
    Path artifact = downloadManager.resolve(buildUrlFor(toolDescriptor, version));
    if (toolDescriptor.packaging().equals(Packaging.TARGZ) || toolDescriptor.packaging().equals(Packaging.TARGZ_STRIP) || toolDescriptor.packaging().equals(Packaging.ZIP)) {
      boolean useRoot = !toolDescriptor.packaging().equals(Packaging.TARGZ_STRIP);
      UnArchiver unArchiver = UnArchiver.builder()
          .useRoot(useRoot)
          .build();
      unArchiver.unarchive(artifact.toFile(), targetDirectory.toFile());
    } else {
      // Copy the single file over and make executable
      Files.copy(artifact, executable, StandardCopyOption.REPLACE_EXISTING);
      executable.toFile().setExecutable(true);
    }
    return ImmutableToolProvisioningResult.builder().executable(executable).build();
  }

  // These belong somewhere else
  public final static ThrowingFunction<Path, ToolDescriptor, IOException> toolDescriptorFrom =
      p -> new YamlMapper<ToolDescriptor>().read(p, ToolDescriptor.class);

  public static List<ToolDescriptor> collectToolDescriptors(Path toolDescriptorDirectory) throws Exception {
    try (Stream<Path> stream = Files.walk(toolDescriptorDirectory, 3)) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toList());
    }
  }

  public static Map<String, ToolDescriptor> collectToolDescriptorsMap() throws Exception {
    try (Stream<Path> stream = Files.walk(toolDescriptorDirectory, 3)) {
      return stream
          .filter(p -> p.toString().endsWith(DESCRIPTOR))
          .map(unchecked(toolDescriptorFrom))
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(), (i, j) -> j, TreeMap::new));
    }
  }
}
