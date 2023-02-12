package ca.vanzyl.provisio.tools.test;

import static ca.vanzyl.provisio.tools.Generators.artifactEntry;
import static ca.vanzyl.provisio.tools.ProfileInstallingTest.validateProfileInstallation;
import static ca.vanzyl.provisio.tools.ProvisioTestSupport.directory;
import static ca.vanzyl.provisio.tools.ProvisioTestSupport.provisio;
import static ca.vanzyl.provisio.tools.model.ProvisioningRequest.TOOL_DESCRIPTOR;
import static ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging.TARGZ;
import static ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging.ZIP;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static ca.vanzyl.provisio.tools.util.FileUtils.writeFile;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static kr.motd.maven.os.Detector.ARCH;
import static kr.motd.maven.os.Detector.OS;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import ca.vanzyl.provisio.archive.generator.ArtifactGenerator;
import ca.vanzyl.provisio.archive.generator.TarGzArtifactGenerator;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ImmutableSyntheticToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfile;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfile.Builder;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import ca.vanzyl.provisio.tools.model.SyntheticToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SyntheticIntegrationTest {
  private int port;
  private Path syntheticRoot;
  private Path remoteRoot;
  private ProvisioningRequest request;
  private YamlMapper<ToolDescriptor> toolDescriptorMapper;
  private YamlMapper<ToolProfile> profileMapper;
  private List<SyntheticToolProfileEntry> syntheticEntries;

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
    syntheticEntries = new ArrayList<>();
  }

  @Test
  public void runningFromEndToEnd() throws Exception {
    testModeOn();
    // This needs nice builder to comprehensive archive content
    testEntry("tool001", "1.0.1", TARGZ, "file");
    testEntry("tool002", "1.0.2", TARGZ, "file");
    testEntry("tool003", "1.0.3", ZIP, "file");

    toolProfile();
    generateToolArtifacts();
    startServingToolArtifacts();
    provisio(request.provisioRoot(), request.userProfile()).installProfile();
    stopServingToolArtifacts();
    validateProfileInstallation(request);
    testModeOff();
  }

  private void testModeOn() {
    System.setProperty("provisio-test-mode", "true");
  }

  private void testModeOff() {
    System.setProperty("provisio-test-mode", "false");
  }

  private void testEntry(String toolId, String version, Packaging packaging, String layout) {
    syntheticEntries.add(ImmutableSyntheticToolProfileEntry.builder()
        .name(toolId)
        .version(version)
        .packaging(packaging)
        .layout(layout)
        .addAllArtifactEntries(List.of(artifactEntry(toolId, "#!/bin/sh")))
        .build());
  }

  private void toolProfile() throws IOException {
    Builder profileBuilder = ImmutableToolProfile.builder();
    for(SyntheticToolProfileEntry e : syntheticEntries) {
      profileBuilder.putTools(e.name(), ImmutableToolProfileEntry.builder()
          .name(e.name()).version(e.version()).build());
    }
    writeFile(request.userProfileYaml(), profileMapper.write(profileBuilder.build()));
  }

  //
  // This is simplistic but will serve as a start. We need something where we can introduce
  // errors and validate the errors recovered from or handled correctly.
  //
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
    for(SyntheticToolProfileEntry e : syntheticEntries) {
      ToolDescriptor toolDescriptor = toolDescriptor(e);
      String toolDescriptorContent = toolDescriptorMapper.write(toolDescriptor);
      System.out.println(toolDescriptorContent);
      Path toolDescriptorPath = request.toolDescriptorsDirectory().resolve(e.name()).resolve(TOOL_DESCRIPTOR);
      writeFile(toolDescriptorPath, toolDescriptorContent);
      String fileName = format("%s-%s-%s-%s.%s", e.name(), OS, ARCH, e.version(), e.extension());
      File artifactLayoutDirectory = syntheticRoot.resolve("build").resolve(e.name()).toFile();
      Path targetArchive = remoteRoot.resolve(fileName);
      createDirectories(targetArchive.getParent());
      ArtifactGenerator generator = new TarGzArtifactGenerator(targetArchive.toFile(), artifactLayoutDirectory, e.artifactEntries());
      generator.generate();
    }
  }

  private ToolDescriptor toolDescriptor(SyntheticToolProfileEntry e) {
    return ImmutableToolDescriptor.builder()
        .id(e.name())
        .name(e.name())
        .layout(e.layout())
        .packaging(e.packaging())
        .executable(e.name())
        .defaultVersion(e.version())
        .urlTemplate(url(e.name(), e.extension()))
        .build();
  }
}
