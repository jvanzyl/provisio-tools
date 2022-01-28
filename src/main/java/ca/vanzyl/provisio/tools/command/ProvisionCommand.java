package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;

@Command(name = "provision", mixinStandardHelpOptions = true)
public class ProvisionCommand extends CommandWithProfileSupport implements Runnable {

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profileValue());
      provisio.installProfile();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
