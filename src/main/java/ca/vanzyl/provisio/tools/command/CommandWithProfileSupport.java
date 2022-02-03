package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.util.Optional;
import picocli.CommandLine.Parameters;

public abstract class CommandWithProfileSupport {

  @Parameters(defaultValue = "default", description = "Profile to use")
  protected Optional<String> profile;

  protected ProvisioningRequest profileValue() {
    return ImmutableProvisioningRequest.builder().userProfile(profile.orElse(null)).build();
  }
}
