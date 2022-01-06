package ca.vanzyl.provisio.tools.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolProfileEntry.class)
public abstract class ToolProfileEntry {

  public abstract String version();

}