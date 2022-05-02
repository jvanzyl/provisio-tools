package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.generator.ToolDescriptorGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "profile", mixinStandardHelpOptions = true)
public class ProfileCommand extends ProvisioCommandSupport {

  @Option(names = {"-a", "--add"}, description = "URL to analyze and from which to generate a tool descriptor.")
  String url;

  @Override
  public void execute() throws Exception {


    ToolDescriptorGenerator generator = new ToolDescriptorGenerator();
    generator.analyzeAndGenerate(url);
  }
}
