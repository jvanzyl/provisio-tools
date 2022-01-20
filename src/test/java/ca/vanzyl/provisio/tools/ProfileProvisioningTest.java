package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectoryIfExists;

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
    deleteDirectoryIfExists(provisio.userProfileDirectory());
    ToolProfileProvisioningResult result = provisio.provisionProfile();

    // A lot to check to make sure the installation is good

    // Profiles with all tools as an integration test
    // Wrong permission on the disk and warning/correcting
    // No space left on disk and warning/correcting
    // Switching profiles that files are changed correctly
    // Adding profile.shell
    // Upgrading from old provisio
  }
}
