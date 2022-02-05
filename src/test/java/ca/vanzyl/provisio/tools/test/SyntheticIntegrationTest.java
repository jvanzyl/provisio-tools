package ca.vanzyl.provisio.tools.test;

import static ca.vanzyl.provisio.tools.ProfileInstallingTest.validateProfileInstallation;
import static ca.vanzyl.provisio.tools.Provisio.ARCH;
import static ca.vanzyl.provisio.tools.Provisio.OS;
import static ca.vanzyl.provisio.tools.ProvisioTestSupport.directory;
import static ca.vanzyl.provisio.tools.ProvisioTestSupport.provisio;
import static ca.vanzyl.provisio.tools.model.ProvisioningRequest.TOOL_DESCRIPTOR;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static ca.vanzyl.provisio.tools.util.FileUtils.writeFile;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import ca.vanzyl.provisio.archive.generator.ArtifactGenerator;
import ca.vanzyl.provisio.archive.generator.TarGzArtifactGenerator;
import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfile;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfile.Builder;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class SyntheticIntegrationTest {

  private int port;
  private Path syntheticRoot;
  private Path remoteRoot;
  private ProvisioningRequest request;
  private YamlMapper<ToolDescriptor> toolDescriptorMapper;
  private YamlMapper<ToolProfile> profileMapper;

  @Before
  public void setUp() throws Exception {
    port = 4567;
    syntheticRoot = directory("synthetic");
    resetDirectory(syntheticRoot);
    remoteRoot = syntheticRoot.resolve("remote");
    request = ImmutableProvisioningRequest.builder()
        .userProfile("test")
        .provisioRoot(syntheticRoot.resolve(".provisio"))
        .build();
    toolDescriptorMapper = new YamlMapper<>();
    profileMapper = new YamlMapper<>();
  }

  // Create profiles for testing
  // We need to easily make new profiles and updated versions of profile to test mutation of profile
  protected Path toolProfile(ToolProfile toolProfile) throws Exception {
    YamlMapper<ToolProfile> mapper = new YamlMapper<>();
    String toolProfileContent = mapper.write(toolProfile);
    Path toolProfilePath = null;
    Files.writeString(toolProfilePath, toolProfileContent);
    return toolProfilePath;
  }

  @Test
  public void runningFromEndToEnd() throws Exception {
    // Create the tool profile
    Builder profileBuilder = ImmutableToolProfile.builder();
    entry(profileBuilder, "tool001", "1.0.1");
    //entry(profileBuilder, "tool002", "1.0.2");
    String profileYamlContent = profileMapper.write(profileBuilder.build());
    System.out.println(profileYamlContent);

    Path profile = request.userProfileYaml();
    writeFile(profile, profileYamlContent);

    generateToolArtifacts();
    startServingToolArtifacts();

    Provisio provisio = provisio(request.provisioRoot(), request.userProfile());
    provisio.installProfile();
    stopServingToolArtifacts();

    validateProfileInstallation(request);
  }

  private void entry(Builder profileBuilder, String id, String version) {
    profileBuilder.putTools(id, ImmutableToolProfileEntry.builder().name(id).version(version).build());
  }

  private void startServingToolArtifacts() {
    port(port);
    staticFiles.expireTime(600);
    externalStaticFileLocation(remoteRoot.toString());
    get("/status", (req, res) -> "OK");
  }

  private String url(String toolId, String extension) {
    return format("http://localhost:%s/%s-{os}-{arch}-{version}.%s", port, toolId, extension);
  }

  private void stopServingToolArtifacts() {
    stop();
  }

  // So these have to have some form of executable
  private void generateToolArtifacts() throws IOException {
    String arch = ARCH;
    String os = OS;

    String toolId = "tool001";
    String extension = "tar.gz";
    String version = "1.0.1";

    ToolDescriptor toolDescriptor = ImmutableToolDescriptor.builder()
        .id(toolId)
        .name(toolId)
        .layout("file")
        .packaging(Packaging.TARGZ)
        .executable(toolId)
        .defaultVersion(version)
        .urlTemplate(url(toolId, extension))
        .build();

    String toolDescriptorContent = toolDescriptorMapper.write(toolDescriptor);
    System.out.println(toolDescriptorContent);
    Path toolDescriptorPath = request.toolDescriptorsDirectory().resolve(toolId).resolve(TOOL_DESCRIPTOR);
    writeFile(toolDescriptorPath, toolDescriptorContent);

    String fileName = format("%s-%s-%s-%s.%s", toolId, os, arch, version, extension);
    System.out.println(fileName);

    File artifactLayoutDirectory = syntheticRoot.resolve("build").toFile();
    Path targetArchive = remoteRoot.resolve(fileName);
    createDirectories(targetArchive.getParent());
    ArtifactGenerator generator = new TarGzArtifactGenerator(targetArchive.toFile(), artifactLayoutDirectory)
        .entry("1/one.txt", "one")
        .entry("2/two.txt", "two")
        .entry("3/three.txt", "three");
    generator.generate();
  }

}
