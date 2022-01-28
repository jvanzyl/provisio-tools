package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import picocli.CommandLine.Command;

@Command(name = "install", mixinStandardHelpOptions = true)
public class InstallCommand extends CommandWithProfileSupport implements Runnable {

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profileValue());
      ToolProfileProvisioningResult result = provisio.installProfile();
      if(!result.provisioningSuccessful()) {
        System.out.println(result.errorMessage());
        System.exit(1);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
