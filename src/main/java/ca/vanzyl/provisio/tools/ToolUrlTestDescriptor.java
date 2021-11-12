package ca.vanzyl.provisio.tools;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolDescriptor.class)
public abstract class ToolUrlTestDescriptor {

  public abstract String id();

  public abstract String version();

  public abstract String url();
}