package ca.vanzyl.provisio.tools;

import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolProvisioningResult {

  public abstract Path executable();

}
