package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolUrlBuilder.cachePathFor;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import org.junit.Test;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class SingleBinaryProvisioningTest extends ProvisioningTestSupport {

  // TARGZ with single entry
  // TARGZ
  // TARGZ_STRIP
  // ZIP
  // FILE

  @Test
  public void provisioningBinaryFromTarGzWithSingleEntry() throws Exception {
    // There are some archives published that only contain a single entry that needs to be extracted. The archive can contain
    // binaries for multiple platforms where we are only interested in extracting the binary for the particular platform,
    // or the single binary is just packaged oddly, and we need to know the entry name entry in order to rename it.
    //
    // -rwxr-xr-x  0 runner docker 6581936 Jun 12  2021 ./yq_darwin_amd64
    //
    validateToolProvisioning("yq", "4.9.6", "yq_darwin_amd64.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGz() throws Exception {
    // -rw-r--r--  0 runner docker     1069 Mar  7  2021 LICENSE
    // -rw-r--r--  0 runner docker     9249 Mar  7  2021 README.md
    // -rwxr-xr-x  0 runner docker 12224056 Mar  7  2021 dive
    validateToolProvisioning("dive", "0.10.0", "dive_0.10.0_darwin_amd64.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGzStrip() throws Exception {
    // drwxr-xr-x  0 circleci circleci        0 Jul 14 14:59 darwin-amd64/
    // -rwxr-xr-x  0 circleci circleci 50093232 Jul 14 14:59 darwin-amd64/helm
    // -rw-r--r--  0 circleci circleci    11373 Jul 14 14:59 darwin-amd64/LICENSE
    // -rw-r--r--  0 circleci circleci     3367 Jul 14 14:59 darwin-amd64/README.md
    validateToolProvisioning("helm", "3.6.3", "helm-v3.6.3-darwin-amd64.tar.gz");
  }

  @Test
  public void provisioningBinaryFromZip() throws Exception {
    validateToolProvisioning("terraform", "0.15.3", "terraform_0.15.3_darwin_amd64.zip");
  }

  @Test
  public void provisioningBinaryFromFile() throws Exception {
    validateToolProvisioning("argocd", "2.1.7", "argocd-darwin-amd64");
  }

  public void validateToolProvisioning(String tool, String version, String fileNameInCache) throws Exception {
    ToolProvisioningResult result = provisio.provisionTool(tool, version);
    assertThat(cachePathFor(provisio.cacheDirectory(), provisio.tool(tool), version)).exists().isRegularFile().hasFileName(fileNameInCache);
    assertThat(result.executable()).exists().isExecutable().hasFileName(tool);
  }
}
