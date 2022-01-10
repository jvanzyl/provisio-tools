package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "provision", mixinStandardHelpOptions = true)
public class ProvisionCommand implements Runnable {

  @Parameters(index = "0", description = "Provision a profile")
  private String profile;

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profile);
      provisio.provisionProfile();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
