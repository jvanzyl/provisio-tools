package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolUrlBuilder.cachePathFor;
import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class InstallationProvisioningTest extends ProvisioningTestSupport {

  @Test
  public void provisioningInstallationUsingZipJunk() throws Exception {
    //
    // jbang
    // └── 0.79.0
    //     ├── jbang
    //     ├── jbang.cmd
    //     ├── jbang.jar
    //     └── jbang.ps1
    //
    validateInstallationProvisioning("jbang", "0.79.0", "jbang.zip",
        withPaths(
            "jbang",
            "jbang.cmd",
            "jbang.jar",
            "jbang.ps1"));
  }

  @Test
  public void provisioningInstallationUsingTarGzStripFromApiEndpoint() throws Exception {
    // The Adoptium JDKs retrieved from an API endpoint and have no file names that can be gleaned from the url.
    validateInstallationProvisioning("java", "jdk-11.0.12+7", "OpenJDK11U-jdk_x64_mac_hotspot_11.0.12_7.tar.gz",
        withPaths(
            "Contents/Home/release",
            "Contents/Home/bin/java",
            "Contents/Home/bin/unpack200",
            "Contents/Home/jmods/java.base.jmod"));
  }

  protected void validateInstallationProvisioning(String tool, String version, String fileNameInCache, List<String> paths) throws Exception {
    ToolProvisioningResult result = provisio.provisionTool(tool, version);
    Path fileInCache = cachePathFor(provisio.cacheDirectory(), provisio.tool(tool), version, fileNameInCache);
    assertThat(fileInCache).exists().isRegularFile().hasFileName(fileNameInCache);
    assertThat(result.installation()).exists().isDirectory().hasFileName(version);
    paths.forEach(path -> assertThat(requireNonNull(result.installation()).resolve(path)).exists());
  }

  protected List<String> withPaths(String... paths) {
    return List.of(paths);
  }
}
