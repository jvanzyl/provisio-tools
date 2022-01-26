package ca.vanzyl.provisio.tools.command;

import java.util.Optional;
import picocli.CommandLine.Parameters;

public abstract class CommandWithProfileSupport {

  @Parameters(defaultValue = "default", description = "Profile to use")
  protected Optional<String> profile;

  protected String profileValue() {
    String profileValue = profile.orElse("default");
    return !profileValue.equals("default") ? profileValue : null;
  }
}
