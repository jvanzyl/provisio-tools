package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.Provisio.PROFILE_YAML;
import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileInstallingTest extends ProvisioTestSupport {

  @Test
  public void initializingProvisioRoot() throws Exception {
    provisio.initialize();
  }

  @Test
  @Ignore
  public void validateProfileInstallation() throws Exception {
    deleteDirectoryIfExists(userBinaryProfileDirectory());
    ToolProfileProvisioningResult result = provisio.installProfile();
    if(!result.provisioningSuccessful()) {
      fail(result.errorMessage());
    }

    // ----------------------------------------------------------------
    // 1) .provisio
    // 2) ├── bin
    //    │   ├── cache
    //    |   ├── installs
    //    │   └── profiles
    //    |       ├── current (should contain the content "test")
    //    |       ├── profile -> test
    //    |       └── test
    //    ├── libexec
    // 3) │   ├── darwin.bash
    //    │   └── provisio-functions.bash
    // 4) ├── profiles
    //    |   └── test
    // 5) ├── provisio -> bin/installs/provisio/0.0.17/provisio
    // 6) └── tools
    //        ├── argocd
    //        ├── aws-cli
    //        ├── aws-iam-authenticator
    //        └── bats
    // ----------------------------------------------------------------

    // 1)
    assertThat(request.provisioRoot()).exists();
    // 2)
    assertThat(request.binDirectory()).exists();
    assertThat(request.cacheDirectory()).exists();
    assertThat(request.installsDirectory()).exists();
    assertThat(request.binaryProfilesDirectory()).exists();
    assertThat(request.binaryProfileDirectory().resolve(PROFILE_YAML)).exists();
    assertThat(request.binaryProfilesDirectory().resolve("current")).exists().content().isEqualTo(userProfile);
    assertThat(request.binaryProfilesDirectory().resolve("profile")).exists().isSymbolicLink();
    assertThat(request.binaryProfilesDirectory().resolve("profile").toRealPath().getFileName().toString()).isEqualTo(userProfile);
    // 3)
    assertThat(request.provisioRoot().resolve("libexec")).exists();
    assertThat(request.provisioRoot().resolve("libexec").resolve("darwin.bash")).exists();
    assertThat(request.provisioRoot().resolve("libexec").resolve("provisio-functions.bash")).exists();
    // 4)
    assertThat(request.userProfilesDirectory()).exists();
    assertThat(request.userProfilesDirectory().resolve(userProfile)).exists();
    // 5) This will not exist during tests but should be present in integration tests
    assertThat(request.provisioRoot().resolve("provisio")).exists();
    // 6)
    assertThat(request.provisioRoot().resolve("tools")).exists();

    // generate profiles
    // mutate profiles
    // check updates
    // webserver that generates all types of artifacts so I can do everything from scratch
    // check content disposition header usage
    // script generation


    // A lot to check to make sure the installation is good
    // updating a profile so if versions change or configurations change the update is performed
    // Profiles with all tools as an integration test
    // Wrong permission on the disk and warning/correcting
    // No space left on disk and warning/correcting
    // Switching profiles that files are changed correctly
    // test the profile.shell additions are added correctly
  }

  @Test
  public void newVersionOfToolIsMadeAvailableCorrectly() throws Exception {

  }

  @Test
  public void selfUpdating() throws Exception {
    provisio.selfUpdate();
  }
}
