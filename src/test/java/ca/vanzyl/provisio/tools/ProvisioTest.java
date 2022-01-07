package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.FileUtils.resetDirectory;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.cachePathForTool;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProvisioTest {

  protected Provisio provisio;

  @Before
  public void setUp() throws Exception {
    Path target = Paths.get("target", "provisio");
    Path cacheDirectory = target.resolve("cache");
    Path binaryDirectory = target.resolve("bin");
    resetDirectory(binaryDirectory);
    provisio = new Provisio(cacheDirectory, binaryDirectory);
  }

  @Test
  public void validateToolProvisioningUsingTarGzWithSingleEntry() throws Exception {
    // There are some archives published that only contain a single entry that needs to be extracted. The archive can contain
    // binaries for multiple platforms where we are only interested in extracting the binary for the particular platform,
    // or the single binary is just packaged oddly, and we need to know the entry name entry in order to rename it.
    //
    // -rwxr-xr-x  0 runner docker 6581936 Jun 12  2021 ./yq_darwin_amd64
    //
    validateToolProvisioning("yq", "4.9.6", "yq_darwin_amd64.tar.gz");
  }

  @Test
  public void validateToolProvisioningUsingTarGz() throws Exception {
    // -rw-r--r--  0 runner docker     1069 Mar  7  2021 LICENSE
    // -rw-r--r--  0 runner docker     9249 Mar  7  2021 README.md
    // -rwxr-xr-x  0 runner docker 12224056 Mar  7  2021 dive
    validateToolProvisioning("dive", "0.10.0", "dive_0.10.0_darwin_amd64.tar.gz");
  }

  @Test
  public void validateToolProvisioningUsingTarGzStrip() throws Exception {
    // drwxr-xr-x  0 circleci circleci        0 Jul 14 14:59 darwin-amd64/
    // -rwxr-xr-x  0 circleci circleci 50093232 Jul 14 14:59 darwin-amd64/helm
    // -rw-r--r--  0 circleci circleci    11373 Jul 14 14:59 darwin-amd64/LICENSE
    // -rw-r--r--  0 circleci circleci     3367 Jul 14 14:59 darwin-amd64/README.md
    validateToolProvisioning("helm", "3.6.3", "helm-v3.6.3-darwin-amd64.tar.gz");
  }


  public void validateToolProvisioning(String tool, String version, String fileName) throws Exception {
    ToolProvisioningResult result = provisio.provisionTool(tool, version);
    assertThat(result.executable()).exists().isExecutable().hasFileName(tool);
    assertThat(cachePathForTool(provisio.cacheDirectory(), provisio.tool(tool), version)).exists().isRegularFile().hasFileName(fileName);
  }

  @Test
  @Ignore
  public void validateProfileProvisioning() throws Exception {
    Path target = Paths.get("target", "provisio");
    Path bin = target.resolve("bin");
    resetDirectory(bin);

    Provisio provisio = new Provisio(Provisio.cache, bin);
    ToolProfileProvisioningResult result = provisio.provisionProfile("jvanzyl");

    // resuse the cache for tests
    // assert yq binary exists in ./bin directory
    // assert yq archive is in the cache
  }
}
