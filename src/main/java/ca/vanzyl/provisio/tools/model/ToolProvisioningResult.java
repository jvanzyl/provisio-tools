package ca.vanzyl.provisio.tools.model;

import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolProvisioningResult {

  public abstract Path executable();

}
