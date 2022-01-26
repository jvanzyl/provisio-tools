package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "provision", mixinStandardHelpOptions = true)
public class ProvisionCommand extends CommandWithProfileSupport implements Runnable {

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profileValue());
      provisio.provisionProfile();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
