package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.generator.ToolDescriptorGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

// TODO: various utilties to list tools, add tools to a profile

@Command(name = "profile", mixinStandardHelpOptions = true)
public class ProfileUpdateCommand implements Runnable {

  @Option(names = {"-u", "--url"}, description = "URL to analyze and from which to generate a tool descriptor.")
  String url;

  @Override
  public void run() {
    try {
      ToolDescriptorGenerator generator = new ToolDescriptorGenerator();
      generator.analyzeAndGenerate(url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
