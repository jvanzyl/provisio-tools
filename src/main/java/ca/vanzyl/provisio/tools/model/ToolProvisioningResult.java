package ca.vanzyl.provisio.tools.model;

import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolProvisioningResult {

  @Nullable
  public abstract Path executable();

  @Nullable
  public abstract Path installation();

  @Nullable
  public abstract List<Path> paths();
}
