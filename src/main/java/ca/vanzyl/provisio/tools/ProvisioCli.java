package ca.vanzyl.provisio.tools;

import picocli.CommandLine.Command;

@Command(name = "provisio", mixinStandardHelpOptions = true)
public class ProvisioCli implements Runnable {

  @Override
  public void run() {
    System.out.println("Hello!");
  }
}
