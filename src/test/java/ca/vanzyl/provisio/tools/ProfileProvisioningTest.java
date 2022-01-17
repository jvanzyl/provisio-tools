package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.util.FileUtils.deleteDirectory;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileProvisioningTest extends ProvisioTestSupport {

  @Test
  @Ignore
  public void validateProfileProvisioning() throws Exception {
    // We need to be able to do this repeatedly
    deleteDirectory(provisio.userProfileDirectory());
    ToolProfileProvisioningResult result = provisio.provisionProfile();

    // A lot to check to make sure the installation is good
  }
}
