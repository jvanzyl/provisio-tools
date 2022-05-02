package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;

@Command(name = "provision", mixinStandardHelpOptions = true)
public class ProvisionCommand extends ProvisioCommandSupport {

  @Override
  public void execute() throws Exception {
    Provisio provisio = provisio();
    provisio.installProfile();
  }
}
