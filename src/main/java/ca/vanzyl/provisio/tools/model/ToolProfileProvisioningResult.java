package ca.vanzyl.provisio.tools.model;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolProfileProvisioningResult {

  public abstract List<ToolProvisioningResult> tools();

}
