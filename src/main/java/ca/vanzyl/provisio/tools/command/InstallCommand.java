package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.model.ToolProfileProvisioningResult;
import picocli.CommandLine.Command;

@Command(name = "install", mixinStandardHelpOptions = true)
public class InstallCommand extends ProvisioCommandSupport {

  @Override
  public void execute() throws Exception {
      Provisio provisio = provisio();
      ToolProfileProvisioningResult result = provisio.installProfile();
      if(!result.provisioningSuccessful()) {
        System.out.println(result.errorMessage());
        System.exit(1);
      }
  }
}
