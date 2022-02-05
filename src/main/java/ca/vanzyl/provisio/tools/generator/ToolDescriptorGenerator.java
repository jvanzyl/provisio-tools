package ca.vanzyl.provisio.tools.generator;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static java.nio.file.Files.*;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

import ca.vanzyl.provisio.archive.UnArchiver;
import ca.vanzyl.provisio.tools.generator.github.GitHubReleaseSource;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor.Builder;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// We need to collect download urls for a given tool
// A GitHub release pages is a source of download urls
// Tools like

// https://en.wikipedia.org/wiki/Uname

public class ToolDescriptorGenerator {

  private final List<ReleaseSource> releaseSources;

  public ToolDescriptorGenerator() {
    releaseSources = List.of(new GitHubReleaseSource());
  }

  public void generate(String url) throws Exception {
    ReleaseInfo info = null;
    System.out.println("Attempting to analyze " + url + " ...");
    for (ReleaseSource s : releaseSources) {
      if (s.canProcess(url)) {
        info = s.info(url);
      }
    }

    if (info == null) {
      throw new Exception("Cannot find release analyzer for " + url);
    }

    analyze(info);
  }

  public void analyze(ReleaseInfo info) throws Exception {

    Map<String, String> osMappings = new HashMap<>();
    Map<String, String> archMappings = new HashMap<>();

    String urlToAnalyze = null;
    String foundOsIdentifier = null;
    String foundArchIdentifier = null;

    for (String downloadUrl : info.urls()) {
      System.out.println(downloadUrl);

      // How to analyze these better, really I can pull them out of existing descriptors, as they serve
      // as the real examples

      // OS mappings
      if (downloadUrl.contains("darwin")) {
        osMappings.put("Darwin", "darwin");
        foundOsIdentifier = "darwin";
        urlToAnalyze = downloadUrl;
      } else if (downloadUrl.contains("macOS")) {
        osMappings.put("Darwin", "macOS");
        foundOsIdentifier = "macOS";
        urlToAnalyze = downloadUrl;
      } else if (downloadUrl.contains("linux")) {
        osMappings.put("Linux", "linux");
      }

      // Arch mappings
      if (downloadUrl.contains("x64")) {
        archMappings.put("x86_64", "x64");
        foundArchIdentifier = "x64";
      } else if (downloadUrl.contains("amd64")) {
        archMappings.put("x86_64", "amd64");
        foundArchIdentifier = "amd64";
      } else if (downloadUrl.contains("arm64")) {
        archMappings.put("arm64", "arm");
        foundArchIdentifier = "arm64";
      }
    }

    Path provisioRoot = Paths.get(System.getProperty("user.home"), ".provisio");
    Path cache = provisioRoot.resolve("bin").resolve("analyze");

    Path tmpdir = Paths.get("target").toAbsolutePath().resolve("analyze");
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

    String version = info.version().replace("v", "");
    String urlTemplate = urlToAnalyze
        .replace(foundOsIdentifier, "{os}")
        .replace(foundArchIdentifier, "{arch}")
        .replace(version, "{version}");

    String toolId;
    String toolIdFromFile;
    if(fileName.indexOf("-") > 0) {
      toolIdFromFile = fileName.indexOf("-") > 0 ? fileName.substring(0, fileName.indexOf("-")) : fileName;
    } else if (fileName.indexOf("_") > 0) {
      toolIdFromFile = fileName.indexOf("_") > 0 ? fileName.substring(0, fileName.indexOf("_")) : fileName;
    } else {
      toolIdFromFile = fileName;
    }
    String toolIdFromInfo = info.name();
    if (toolIdFromInfo.equals(toolIdFromFile)) {
      toolId = toolIdFromInfo;
    } else {
      toolId = toolIdFromFile;
    }
    System.out.println("toolNameFromFile = " + toolIdFromFile);

    Builder builder = ImmutableToolDescriptor.builder();
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
        builder.layout("directory");
        // Now step into the directory and see if there is a bin/ directory
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

    } else if (artifact.getFileName().endsWith(".xz")) {

    } else {
      builder.packaging(Packaging.FILE);
      builder.layout("file");
    }

    builder.osMappings(osMappings);
    builder.archMappings(archMappings);
    builder.defaultVersion(version);
    builder.executable(toolId);
    builder.urlTemplate(urlTemplate);

    System.out.println("Generating tool descriptor ...");
    System.out.println();
    YamlMapper<ToolDescriptor> yamlMapper = new YamlMapper<>();
    System.out.println(yamlMapper.write(builder.build()));
  }

  public static void main(String[] args) throws Exception {
    ToolDescriptorGenerator generator = new ToolDescriptorGenerator();

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
    //generator.analyze(info);

    // Pulumi
    //generator.generate("https://github.com/pulumi/pulumi/releases");

    // GitHub CLI
    generator.generate("https://github.com/cli/cli/releases");
  }
}
