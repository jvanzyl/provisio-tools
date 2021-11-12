package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolDescriptor.DESCRIPTOR;
import static com.pivovarit.function.ThrowingFunction.unchecked;

import ca.vanzyl.provisio.tools.ToolDescriptor.Packaging;
import com.pivovarit.function.ThrowingFunction;
import io.tesla.proviso.archive.UnArchiver;
import java.io.IOException;
import java.net.URI;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import kr.motd.maven.os.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// This has everything to do with initializing a tool for use in
//
// terraform:
// https://releases.hashicorp.com/terraform/%s/terraform_%s_%s_amd64.zip
//
// eksctl
// https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz"
//
// helm:
// https://get.helm.sh/helm-v2.14.3-linux-amd64.tar.gz
//
// kubectl:
// https://storage.googleapis.com/kubernetes-release/release/v1.16.0/bin/linux/amd64/kubectl
//
// packaging = file, tar.gz, zip
// urlTemplate
// architectureMapper
// where to place it in the workingDirectory
//
@Named
@Singleton
public class ToolProvisioner {

  private static final Logger logger = LoggerFactory.getLogger(ToolProvisioner.class);
  public static final String OS = Detector.normalizeOs(System.getProperty("os.name"));
  public static final String ARCH = Detector.normalizeArch(System.getProperty("os.arch"));
  public static final Path PROVISIO_ROOT= Paths.get(System.getProperty("user.home"),".provisio/");

  private final DownloadManager downloadManager;
  private final boolean debug = true;

  @Inject
  public ToolProvisioner(DownloadManager dependencyManager) {
    this.downloadManager = dependencyManager;
  }

  public ToolProvisioningResult provision(Path workDir, ToolDescriptor toolDescriptor) throws Exception {
    return provision(workDir, toolDescriptor, null);
  }

  public ToolProvisioningResult provision(Path workDir, ToolDescriptor toolDescriptor, String version) throws Exception {

    String executable = toolDescriptor.executable();

    // We will check our new setup where the agent image has the executables we need
    Path concordHomeExecutablePath = Paths.get("/home/concord/bin").resolve(executable);
    if (Files.exists(concordHomeExecutablePath)) {
      if (debug) {
        logger.info("init -> using the existing binary on the agent image in {}", workDir.relativize(concordHomeExecutablePath));
      }
      return ImmutableToolProvisioningResult.builder().executable(concordHomeExecutablePath).build();
    }

    Path targetDirectory = workDir.resolve("." + toolDescriptor.id()); // .eksctl, .terraform, .helm, etc

    if (!Files.exists(targetDirectory)) {
      Files.createDirectories(targetDirectory);
    }

    Path executablePath = targetDirectory.resolve(executable);
    if (Files.exists(executablePath)) {
      if (debug) {
        logger.info("init -> using the existing binary {}", workDir.relativize(executablePath));
      }
      return ImmutableToolProvisioningResult.builder().executable(executablePath).build();
    }

    String toolUrl = ToolUrlBuilder.build(toolDescriptor, version);
    logger.info("Retrieving {} package from {} ...", toolDescriptor.name(), toolUrl);
    Path executablePackage = downloadManager.resolve(new URI(toolUrl));
    logger.info("Retrieved {} package and saved to {} ...", toolDescriptor.name(), executablePackage);

    if (debug) {
      logger.info("init -> extracting the executable into {}", workDir.relativize(targetDirectory));
    }

    if (executablePackage == null) {
      throw new IllegalStateException(String.format("The Terraform archive '%s' does not appear to be valid.", executablePackage));
    }

    if (toolDescriptor.packaging().equals(Packaging.TARGZ) || toolDescriptor.packaging().equals(Packaging.TARGZ_STRIP) || toolDescriptor.packaging().equals(Packaging.ZIP)) {
      boolean useRoot = !toolDescriptor.packaging().equals(Packaging.TARGZ_STRIP);
      logger.info("Unarchiving {} to {} ...", executablePackage.getFileName(), targetDirectory);
      UnArchiver unArchiver = UnArchiver.builder()
          .useRoot(useRoot)
          .build();
      unArchiver.unarchive(executablePackage.toFile(), targetDirectory.toFile());
    } else {
      // Copy the single file over and make executable
      Files.copy(executablePackage, executablePath, StandardCopyOption.REPLACE_EXISTING);
      executablePath.toFile().setExecutable(true);
    }

    return ImmutableToolProvisioningResult.builder().executable(executablePath).build();
  }

  public final static Path toolDescriptorDirectory = Paths.get(System.getProperty("user.home"), "/.provisio/tools");

  public final static ThrowingFunction<Path, ToolDescriptor, IOException> toolDescriptorFrom =
      p -> new YamlMapper<ToolDescriptor>().read(p, ToolDescriptor.class);

  public static List<ToolDescriptor> collectToolDescriptors() throws Exception {
    return collectToolDescriptors(toolDescriptorDirectory);
  }

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
          .collect(Collectors.toMap(ToolDescriptor::id, Function.identity(),(i, j) -> j, TreeMap::new));
    }
  }
}
