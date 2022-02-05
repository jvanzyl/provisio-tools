package ca.vanzyl.provisio.tools.test;

import static ca.vanzyl.provisio.tools.Provisio.ARCH;
import static ca.vanzyl.provisio.tools.Provisio.OS;
import static ca.vanzyl.provisio.tools.util.FileUtils.resetDirectory;
import static java.lang.String.format;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import ca.vanzyl.provisio.archive.generator.ArtifactGenerator;
import ca.vanzyl.provisio.archive.generator.TarGzArtifactGenerator;
import ca.vanzyl.provisio.tools.ProvisioTestSupport;
import ca.vanzyl.provisio.tools.model.ImmutableToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class ArtifactServer extends ProvisioTestSupport {

  private int port;
  private Path directoryOfArtifacts;
  private YamlMapper<ToolDescriptor> mapper;

  @Before
  public void setUp() throws Exception {
    port = 4567;
    directoryOfArtifacts = directory("remote-artifacts");
    mapper = new YamlMapper<>();
  }

  @Test
  public void runningFromEndToEnd() throws Exception {
    resetDirectory(directoryOfArtifacts);
    generateRemoteArtifacts();
    generateToolDescriptors();
    startServingRemoteArtifacts();
    // Run provisio and verify the profile
    stopServingRemoteArtifacts();
  }

  private void startServingRemoteArtifacts() throws IOException {
    port(port);
    staticFiles.expireTime(600);
    externalStaticFileLocation(directoryOfArtifacts.toString());
    get("/status", (req, res) -> "OK");
  }

  private String url(String toolId, String extension) {
    return format("http://localhost:%s/%s-{os}-{arch}-{version}.%s", port, toolId, extension);
  }

  private void stopServingRemoteArtifacts() {
    stop();
  }

  private void generateToolDescriptors() throws Exception {
  }

  // So these have to have some form of executable
  private void generateRemoteArtifacts() throws IOException {
    String arch = ARCH;
    String os = OS;

    String toolId = "tool001";
    String extension = "tar.gz";
    String version = "1.0.0";

    ToolDescriptor toolDescriptor = ImmutableToolDescriptor.builder()
        .id(toolId)
        .name(toolId)
        .layout("file")
        .packaging(Packaging.TARGZ)
        .executable(toolId)
        .defaultVersion(version)
        .urlTemplate(url(toolId, extension))
        .build();
    String toolDescriptorYaml = mapper.write(toolDescriptor);
    System.out.println(toolDescriptorYaml);

    String fileName = format("%s-%s-%s-%s.%s", toolId, os, arch, version, extension);
    System.out.println(fileName);

    File artifactLayoutDirectory = directoryOfArtifacts.resolve("build").toFile();
    File targetArchive = directoryOfArtifacts.resolve(fileName).toFile();
    ArtifactGenerator generator = new TarGzArtifactGenerator(targetArchive, artifactLayoutDirectory)
        .entry("1/one.txt", "one")
        .entry("2/two.txt", "two")
        .entry("3/three.txt", "three");
    generator.generate();
  }

}
