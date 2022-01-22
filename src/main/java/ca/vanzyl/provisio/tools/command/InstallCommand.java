package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "install", mixinStandardHelpOptions = true)
public class InstallCommand implements Runnable {

  @Parameters(index = "0", description = "Install a profile")
  private String profile;

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profile);
      ToolProfileProvisioningResult result = provisio.provisionProfile();
      if(!result.provisioningSuccessful()) {
        System.out.println(result.errorMessage());
        System.exit(1);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
