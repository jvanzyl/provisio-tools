package ca.vanzyl.provisio.tools.model;

import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolProfileProvisioningResult {

  @Value.Default
  public boolean provisioningSuccessful() {
    return true;
  }

  @Nullable
  public abstract String errorMessage();

  public abstract List<ToolProvisioningResult> tools();

}
