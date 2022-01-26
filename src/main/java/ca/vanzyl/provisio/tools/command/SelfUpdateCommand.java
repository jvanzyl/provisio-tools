package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;

@Command(name = "selfupdate", mixinStandardHelpOptions = true)
public class SelfUpdateCommand implements Runnable {

  @Override
  public void run() {
    try {
      Provisio provisio = new Provisio();
      provisio.selfUpdate();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
