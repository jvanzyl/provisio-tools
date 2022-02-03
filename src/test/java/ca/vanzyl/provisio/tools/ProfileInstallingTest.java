package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.Provisio.PROFILE_YAML;
import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import java.nio.file.Path;
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

    // .provisio
    // ├── bin
    // ├── libexec
    // ├── profiles
    // ├── provisio -> bin/installs/provisio/0.0.17/provisio
    // └── tools
    //
    assertThat(request.provisioRoot()).exists();
    // This may not be the case if the profile is defined in ${PWD}
    assertThat(request.userProfilesDirectory()).exists();
    assertThat(request.binDirectory()).exists();
    assertThat(request.cacheDirectory()).exists();
    assertThat(request.installsDirectory()).exists();
    assertThat(request.binaryProfilesDirectory()).exists();
    assertThat(request.binaryProfileDirectory().resolve(PROFILE_YAML)).exists();

    // profiles
    // ├── current (should contain the content "jvanzyl")
    // ├── jvanzyl
    // ├── profile -> jvanzyl
    // └── test
    //
    assertThat(request.binaryProfilesDirectory().resolve("current")).exists().content().isEqualTo(userProfile);
    Path profileSymlink = request.binaryProfilesDirectory().resolve("profile");
    assertThat(profileSymlink).exists().isSymbolicLink();
    // https://stackoverflow.com/questions/43720118/java-how-to-find-the-target-file-path-that-a-symbolic-link-points-to
    assertThat(profileSymlink.toRealPath().toString()).endsWith(userProfile);
    // Maybe don't use resolve here because it is a relative symlink that we want to test. Not sure what resolve
    // is doing here. I want to test it's relative and the file name is the user profile. Might be assertj
    //assertThat(profileSymlink.toRealPath()).isRelative();

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
