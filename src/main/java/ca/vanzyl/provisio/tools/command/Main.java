package ca.vanzyl.provisio.tools.command;

import io.quarkus.picocli.runtime.PicocliCommandLineFactory;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

@TopCommand
@CommandLine.Command(
    name = "provisio", mixinStandardHelpOptions = true,
    versionProvider = VersionProviderWithConfigProvider.class,
    subcommands = {
        ProvisionCommand.class,
        InstallCommand.class
    })
public class Main {

  @Produces
  CommandLine getCommandLineInstance(PicocliCommandLineFactory factory) {
    return factory.create();
  }
}

class VersionProviderWithConfigProvider implements IVersionProvider {

  @Override
  public String[] getVersion() {
    String applicationName = ConfigProvider.getConfig().getValue("quarkus.application.name", String.class);
    String applicationVersion = ConfigProvider.getConfig().getValue("quarkus.application.version", String.class);
    return new String[]{String.format("%s %s", applicationName, applicationVersion)};
  }
}