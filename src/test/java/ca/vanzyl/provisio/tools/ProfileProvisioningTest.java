package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;
import static org.assertj.core.api.Fail.fail;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileProvisioningTest extends ProvisioTestSupport {

  @Test
  public void initializingProvisioRoot() throws Exception {
    provisio.initialize();
  }

  @Test
  @Ignore
  public void validateProfileProvisioning() throws Exception {
    // We need to be able to do this repeatedly
    deleteDirectoryIfExists(userProfileDirectory());
    ToolProfileProvisioningResult result = provisio.installProfile();

    if(!result.provisioningSuccessful()) {
      fail(result.errorMessage());
    }
    // A lot to check to make sure the installation is good

    // updating a profile so if versions change or configurations change the update is performed
    // Profiles with all tools as an integration test
    // Wrong permission on the disk and warning/correcting
    // No space left on disk and warning/correcting
    // Switching profiles that files are changed correctly
    // Adding profile.shell
    // Upgrading from old provisio
    // test the profile.shell additions
  }

  @Test
  public void selfUpdating() throws Exception {
    provisio.selfUpdate();
  }
}
