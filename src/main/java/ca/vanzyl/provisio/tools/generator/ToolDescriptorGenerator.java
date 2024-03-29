package ca.vanzyl.provisio.tools.generator;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.list;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;

import ca.vanzyl.provisio.archive.UnArchiver;
import ca.vanzyl.provisio.tools.generator.github.GitHubReleaseSource;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor.Builder;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// We need to collect download urls for a given tool
// A GitHub release pages is a source of download urls
// Tools like

// https://en.wikipedia.org/wiki/Uname

public class ToolDescriptorGenerator {

  private final boolean save;
  private final ProvisioningRequest request;
  private final List<ReleaseSource> releaseSources;

  public ToolDescriptorGenerator(boolean save) {
    this.save = save;
    this.request = ImmutableProvisioningRequest.builder().build();
    this.releaseSources = List.of(new GitHubReleaseSource());
  }

  public ToolDescriptorGenerator() {
    this(false);
  }

  public void analyzeAndGenerate(String url) throws Exception {
    System.out.println("Attempting to analyze " + url + " ...");
    for (ReleaseSource releaseSource : releaseSources) {
      if (releaseSource.canProcess(url)) {
        analyzeAndGenerate(releaseSource.info(url));
        return;
      }
    }
    throw new Exception("Cannot find release analyzer for " + url);
  }

  public void analyzeAndGenerate(ReleaseInfo releaseInfo) throws Exception {

    Map<String, String> osMappings = new HashMap<>();
    Map<String, String> archMappings = new HashMap<>();

    String urlToAnalyze = null;
    String foundOsIdentifier = null;
    String foundArchIdentifier = null;
    String artifactExtension = null;
    String foundOs = null;

    // cosign has multiple binaries in a release, bad
    // a bunch of different binaries
    // a bunch of other files
    // metadata of the release would be useful to consume

    for (String url : releaseInfo.urls()) {
      if (url.endsWith(".rpm") ||
          url.endsWith(".deb") ||
          url.endsWith(".apk") ||
          url.endsWith(".txt") ||
          url.endsWith(".yaml") ||
          url.endsWith(".pub") ||
          url.endsWith(".pem") ||
          url.endsWith(".sbom") ||
          url.endsWith(".sha256sum") ||
          url.endsWith(".sig")) {
        continue;
      }

      System.out.println(url);
      boolean validUrl = false;

      // OS mappings
      if (url.contains("darwin")) {
        foundOsIdentifier = "darwin";
        osMappings.put("Darwin", "darwin");
      } else if (url.contains("Darwin")) {
        foundOsIdentifier = "Darwin";
        osMappings.put("Darwin", "Darwin");
      } else if (url.contains("macOS")) {
        foundOsIdentifier = "macOS";
        osMappings.put("Darwin", "macOS");
      } else if (url.contains("Linux")) {
        foundOsIdentifier = "Linux";
        osMappings.put("Linux", "Linux");
      } else if (url.contains("linux")) {
        foundOsIdentifier = "linux";
        osMappings.put("Linux", "linux");
      } /* else if (url.contains("windows")) {
        osMappings.put("Windows", "windows");
        foundOsIdentifier = "windows";
        validUrl = true;
      } else if (url.contains("Windows")) {
        osMappings.put("Windows", "Windows");
        foundOsIdentifier = "Windows";
        validUrl = true;
      } */

      // TODO: need to detect if an OS requires a different URL template
      // https://github.com/weaveworks/eksctl/releases/download/v0.108.0-rc.0/eksctl_Darwin_amd64.tar.gz
      // https://github.com/weaveworks/eksctl/releases/download/v0.108.0-rc.0/eksctl_Windows_amd64.zip

      // Arch mappings
      if (url.contains("x64")) {
        archMappings.put("x86_64", "x64");
        foundArchIdentifier = "x64";
        validUrl = true;
      } else if (url.contains("x86_64")) {
        archMappings.put("x86_64", "x86_64");
        foundArchIdentifier = "x86_64";
        validUrl = true;
      } else if (url.contains("amd64")) {
        archMappings.put("x86_64", "amd64");
        foundArchIdentifier = "amd64";
        validUrl = true;
      } else if (url.contains("arm64")) {
        archMappings.put("arm64", "arm64");
        foundArchIdentifier = "arm64";
        validUrl = true;
      } else if (url.contains("aarch64")) {
        archMappings.put("arm64", "aarch64");
        foundArchIdentifier = "aarch64";
        validUrl = true;
      } else {
        validUrl = false;
      }

      if(validUrl) {
        urlToAnalyze = url;
      }
    }

    Path provisioRoot = get(System.getProperty("user.home"), ".provisio");
    Path cache = provisioRoot.resolve("bin").resolve("analyze");

    Path tmpdir = get("target").toAbsolutePath().resolve("analyze");
    deleteDirectoryIfExists(tmpdir);
    createDirectories(tmpdir);

    String fileName = urlToAnalyze.substring(urlToAnalyze.lastIndexOf("/") + 1);
    Path artifact = cache.resolve(fileName);
    if (!exists(artifact)) {
      createDirectories(artifact.getParent());
      HttpRequest artifactRequest = HttpRequest.newBuilder()
          .uri(new URI(urlToAnalyze))
          .version(HttpClient.Version.HTTP_2)
          .GET()
          .build();
      HttpClient client = HttpClient.newBuilder()
          .followRedirects(Redirect.ALWAYS)
          .build();
      HttpResponse<Path> artifactResponse = client.send(artifactRequest, BodyHandlers.ofFile(artifact));
    }

    String version = releaseInfo.version().replace("v", "");
    String urlTemplate;
    if(foundOsIdentifier != null) {
      urlTemplate = urlToAnalyze
          .replace(foundOsIdentifier, "{os}")
          .replace(foundArchIdentifier, "{arch}")
          .replace(version, "{version}");
    } else {
      // In the case of tools only for one OS there may not be an os in the release artifact url
      urlTemplate = urlToAnalyze
          .replace(foundArchIdentifier, "{arch}")
          .replace(version, "{version}");
    }

    // TODO: iterate through all the tools and record the source url so I can reconstruct the tool descriptors
    // when they need more information
    String toolId;
    String toolIdFromFile;
    if (fileName.indexOf("-") > 0) {
      toolIdFromFile = fileName.indexOf("-") > 0 ? fileName.substring(0, fileName.indexOf("-")) : fileName;
    } else if (fileName.indexOf("_") > 0) {
      toolIdFromFile = fileName.indexOf("_") > 0 ? fileName.substring(0, fileName.indexOf("_")) : fileName;
    } else {
      toolIdFromFile = fileName;
    }
    String toolIdFromInfo = releaseInfo.name();
    if (!toolIdFromInfo.equals(toolIdFromFile)) {
      toolId = toolIdFromInfo;
    } else {
      toolId = toolIdFromFile;
    }
    System.out.println("toolId = " + toolId);

    Builder builder = ImmutableToolDescriptor.builder();
    builder.sources(releaseInfo.sources());
    builder.id(toolId);
    builder.name(toolId);

    if (fileName.endsWith("tar.gz")) {
      // Unpack the archive an examine the structure, need a way to just inspect the entries
      UnArchiver unArchiver = UnArchiver.builder().build();
      unArchiver.unarchive(artifact.toFile(), tmpdir.toFile());
      List<Path> files = list(tmpdir).collect(Collectors.toList());
      if (files.size() == 1 && isDirectory(files.get(0))) {
        // We have a top-level directory in the tarball
        builder.packaging(Packaging.TARGZ_STRIP);
        // Now step into the directory and see if there is a bin/ directory
        builder.layout("directory");
        Path bin = files.get(0).resolve("bin");
        if (exists(bin)) {
          builder.paths("bin");
        } else {
          builder.paths(".");
        }
      } else {
        builder.packaging(Packaging.TARGZ);
        builder.layout("file");
      }
    } else if (artifact.getFileName().endsWith(".zip")) {
      builder.packaging(Packaging.ZIP);
      builder.layout("directory");
    } else if (artifact.getFileName().endsWith(".xz")) {

    } else {
      builder.packaging(Packaging.FILE);
      builder.layout("file");
      builder.paths(null);
    }

    builder.osMappings(osMappings);
    builder.archMappings(archMappings);
    builder.defaultVersion(version);
    builder.executable(toolId);
    builder.urlTemplate(urlTemplate);

    System.out.println("Generating tool descriptor ...");
    System.out.println();
    YamlMapper<ToolDescriptor> yamlMapper = new YamlMapper<>();
    ToolDescriptor toolDescriptor = builder.build();
    String toolDescriptorYaml = yamlMapper.write(toolDescriptor);
    System.out.println(toolDescriptorYaml);

    // We are working in the source tree and adding them directly
    if (save) {
      Path toolDescriptorDirectory = request.localToolDescriptorsDirectory().resolve(toolDescriptor.id());
      saveGeneratedToolDescriptor(toolDescriptorYaml, toolDescriptorDirectory);
      /*

      Originally to write into the git tree, but I'm not sure this is useful

      Path workingDirectory = get(System.getProperty("user.dir"));
      if (workingDirectory.toString().endsWith("provisio-tools") && exists(workingDirectory.resolve(".git"))) {
        Path tools = workingDirectory.resolve("src/main/resources/provisioRoot/config/tools");
        Path toolDescriptorDirectory = tools.resolve(toolDescriptor.id());
        saveGeneratedToolDescriptor(toolDescriptorYaml, toolDescriptorDirectory);
      }
      */
    }
  }

  // TODO: this needs to be added to a place where it's usable while someone is adding new tools
  private void saveGeneratedToolDescriptor(String toolDescriptorYaml, Path toolDescriptorDirectory) throws IOException {
    Path toolDescriptorFile = toolDescriptorDirectory.resolve(ToolDescriptor.DESCRIPTOR);
    if (exists(toolDescriptorDirectory)) {
      deleteDirectoryIfExists(toolDescriptorDirectory);
    }
    createDirectories(toolDescriptorDirectory);
    writeString(toolDescriptorFile, toolDescriptorYaml);
  }

  public static void main(String[] args) throws Exception {
    ToolDescriptorGenerator generator = new ToolDescriptorGenerator(true);

    // Kubectl
    ReleaseInfo info = ImmutableReleaseInfo.builder()
        .name("kubectl")
        .urls(List.of(
            "https://dl.k8s.io/release/v1.23.0/bin/linux/amd64/kubectl",
            "https://dl.k8s.io/release/v1.23.0/bin/darwin/amd64/kubectl",
            "https://dl.k8s.io/release/v1.23.0/bin/darwin/arm64/kubectl"
        ))
        .version("1.23.0")
        .build();

    generator.analyzeAndGenerate("https://github.com/gitx/gitx/releases");
  }
}
