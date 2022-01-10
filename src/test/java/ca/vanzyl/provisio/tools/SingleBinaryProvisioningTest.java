package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.cachePathFor;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.interpolateToolPath;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

// TODO: Generate all the various packaging types and startup a local webserver. These are really integration tests for tools that exist in the world.

public class SingleBinaryProvisioningTest extends ProvisioningTestSupport {

  // TARGZ with single entry
  // TARGZ
  // TARGZ_STRIP
  // ZIP
  // FILE

  @Test
  public void provisioningBinaryFromTarGzWithSingleEntryWithLeadingDotSlash() throws Exception {
    // There are some archives published that only contain a single entry that needs to be extracted. The archive can contain
    // binaries for multiple platforms where we are only interested in extracting the binary for the particular platform,
    // or the single binary is just packaged oddly, and we need to know the entry name entry in order to rename it.
    //
    // -rwxr-xr-x  0 runner docker 6581936 Jun 12  2021 ./yq_darwin_amd64
    //
    validateToolProvisioning("yq", "4.9.6", "yq_darwin_amd64.tar.gz");
  }

  // Note that with curl/bash/tar/gzip version it works fine with leading "./" for the single entry
  // or none, whereas with the provisio archiver the exact literal entry is required. The velero
  // single entry was "./velero-v1.6.2-darwin-amd64/velero" in the descriptor and it failed in
  // the Java version until leading "./" was removed.

  @Test
  public void provisioningBinaryFromTarGzWithSingleEntryWithNoLeadingDotSlash() throws Exception {
    // -rw-rw-r--  0 1000   1000    10255 Nov 15  2019 velero-v1.6.2-darwin-amd64/LICENSE
    // -rw-rw-r--  0 1000   1000      476 Jul  8  2021 velero-v1.6.2-darwin-amd64/examples/README.md
    // drwxrwxr-x  0 1000   1000        0 Feb 20  2021 velero-v1.6.2-darwin-amd64/examples/minio
    // -rw-rw-r--  0 1000   1000     2599 Feb 20  2021 velero-v1.6.2-darwin-amd64/examples/minio/00-minio-deployment.yaml
    // drwxrwxr-x  0 1000   1000        0 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app
    // -rw-rw-r--  0 1000   1000      521 Nov 15  2019 velero-v1.6.2-darwin-amd64/examples/nginx-app/README.md
    // -rw-rw-r--  0 1000   1000     1237 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app/base.yaml
    // -rw-rw-r--  0 1000   1000     2375 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app/with-pv.yaml
    // -rwxr-xr-x  0 1000   1000 64937056 Jul 20 10:05 velero-v1.6.2-darwin-amd64/velero
    //
    validateToolProvisioning("velero", "1.6.2", "velero-v1.6.2-darwin-amd64.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGz() throws Exception {
    // -rw-r--r--  0 runner docker     1069 Mar  7  2021 LICENSE
    // -rw-r--r--  0 runner docker     9249 Mar  7  2021 README.md
    // -rwxr-xr-x  0 runner docker 12224056 Mar  7  2021 dive
    //
    validateToolProvisioning("dive", "0.10.0", "dive_0.10.0_darwin_amd64.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGzWithRepackagedAwsCli() throws Exception {
    // -rw-r--r--  0 runner docker     1069 Mar  7  2021 LICENSE
    // -rw-r--r--  0 runner docker     9249 Mar  7  2021 README.md
    // -rwxr-xr-x  0 runner docker 12224056 Mar  7  2021 dive
    //
    validateToolProvisioning("aws-cli", "2.4.9", "awscli-darwin-x86_64-2.4.9.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGzStrip() throws Exception {
    // drwxr-xr-x  0 circleci circleci        0 Jul 14 14:59 darwin-amd64/
    // -rwxr-xr-x  0 circleci circleci 50093232 Jul 14 14:59 darwin-amd64/helm
    // -rw-r--r--  0 circleci circleci    11373 Jul 14 14:59 darwin-amd64/LICENSE
    // -rw-r--r--  0 circleci circleci     3367 Jul 14 14:59 darwin-amd64/README.md
    //
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

  @Test
  public void provisioningBinaryFromGitHubTarGzStrip() throws Exception {
    // For script-based tools like jenv and dimg, fetching a tarball directly from source is often sufficient. Github
    // makes revisions of repositories available as tarballs with the api:
    //
    // https://api.github.com/repos/jvanzyl/dimg/tarball/{version}
    validateToolProvisioning("dimg", "master", "jvanzyl-dimg-1.0.1-4-gfa99186.tar.gz");
  }

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

  @Test
  public void provisioningInstallationUsingTarGzStrip() throws Exception {
    validateInstallationProvisioning("graalvm", "21.3.0", "graalvm-ce-java11-darwin-amd64-21.3.0.tar.gz",
        withPaths(
            "Contents/Home/release",
            "Contents/Home/GRAALVM-README.md",
            "Contents/Home/bin/java",
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

  public void validateToolProvisioning(String toolId, String version, String fileNameInCache) throws Exception {
    ToolDescriptor toolDescriptor = provisio.tool(toolId);
    ToolProvisioningResult result = provisio.provisionTool(toolId, version);
    assertThat(cachePathFor(provisio.cacheDirectory(), provisio.tool(toolId), version, fileNameInCache))
        .exists().isRegularFile().hasFileName(fileNameInCache);
    // Now that we are leaving things as-is we have take into account the raw name. These funky archives
    // need to be normalized
    String executable;
    if(toolDescriptor.tarSingleFileToExtract() != null) {
      executable = interpolateToolPath(toolDescriptor.tarSingleFileToExtract(), toolDescriptor, version);
    } else {
      executable = toolDescriptor.executable();
    }
    assertThat(result.installation().resolve(executable)).exists().isExecutable();
  }
}
