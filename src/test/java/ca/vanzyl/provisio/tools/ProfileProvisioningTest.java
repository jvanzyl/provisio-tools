package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.FileUtils.resetDirectory;

import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileProvisioningTest extends ProvisioningTestSupport {

  @Test
  @Ignore
  public void validateProfileProvisioning() throws Exception {
    Path target = Paths.get("target", "provisio");
    Path bin = target.resolve("bin");
    resetDirectory(bin);

    Provisio provisio = new Provisio(Provisio.cache, bin);
    ToolProfileProvisioningResult result = provisio.provisionProfile("jvanzyl");
  }
}
