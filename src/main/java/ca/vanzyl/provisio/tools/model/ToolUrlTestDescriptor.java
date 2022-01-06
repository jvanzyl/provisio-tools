package ca.vanzyl.provisio.tools.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolUrlTestDescriptor.class)
public abstract class ToolUrlTestDescriptor {

  public abstract String id();

  public abstract String version();

  public abstract String url();
}