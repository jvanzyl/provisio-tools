package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.generator.ToolDescriptorGenerator;
import ca.vanzyl.provisio.tools.model.ImmutableProvisioningRequest;
import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.util.Optional;
import picocli.CommandLine.Parameters;

public abstract class ProvisioCommandSupport implements Runnable {

  @Parameters(defaultValue = "unset", description = "Profile to use")
  protected Optional<String> profile;

  protected ProvisioningRequest profileValue() {
    return ImmutableProvisioningRequest.builder().userProfile(profile.get()).build();
  }

  protected Provisio provisio() {
    try {
      return new Provisio(profileValue());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void execute() throws Exception;

  @Override
  public void run() {
    try {
      execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
