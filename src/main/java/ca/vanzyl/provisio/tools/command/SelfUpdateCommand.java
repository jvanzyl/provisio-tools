package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import picocli.CommandLine.Command;

@Command(name = "selfupdate", mixinStandardHelpOptions = true)
public class SelfUpdateCommand extends ProvisioCommandSupport {
  @Override
  public void execute() throws Exception {
    Provisio provisio = provisio();
    provisio.selfUpdate();
  }
}
