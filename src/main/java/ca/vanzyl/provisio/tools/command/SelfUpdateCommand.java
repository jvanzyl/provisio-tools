package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "selfupdate", mixinStandardHelpOptions = true)
public class SelfUpdateCommand implements Runnable {

  @Parameters(index = "0", description = "Provision a profile")
  private String profile;

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio(profile);
      provisio.selfUpdate();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
