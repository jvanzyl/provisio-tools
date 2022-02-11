package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;

// TODO: various utilties to list tools, add tools to a profile

@Command(name = "tool", mixinStandardHelpOptions = true)
public class ToolCommand implements Runnable {

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
