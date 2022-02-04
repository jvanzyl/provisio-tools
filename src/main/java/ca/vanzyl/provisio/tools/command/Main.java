package ca.vanzyl.provisio.tools.command;

import io.quarkus.picocli.runtime.PicocliCommandLineFactory;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import java.net.URL;
import java.util.Properties;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

@TopCommand
@CommandLine.Command(
    name = "provisio", mixinStandardHelpOptions = true,
    versionProvider = PropertiesVersionProvider.class,
    subcommands = {
        ProvisionCommand.class,
        InstallCommand.class,
        SelfUpdateCommand.class
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
    //String applicationVersion = ConfigProvider.getConfig().getValue("quarkus.application.version", String.class);
    String applicationVersion = ConfigProvider.getConfig().getValue("provisio.cli.version", String.class);
    return new String[]{String.format("%s %s", applicationName, applicationVersion)};
  }
}

class PropertiesVersionProvider implements IVersionProvider {
  public String[] getVersion() throws Exception {
    URL url = getClass().getResource("/project.properties");
    if (url == null) {
      return new String[] {"No project.properties file found in the classpath."};
    }
    Properties properties = new Properties();
    properties.load(url.openStream());
    String name = properties.getProperty("name");
    String version = properties.getProperty("version");
    String revision = properties.getProperty("shortRevision");
    return new String[]{String.format("%s (%s)", version, revision)};
  }
}