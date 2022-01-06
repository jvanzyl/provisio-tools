package ca.vanzyl.provisio.tools.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolProfile.class)
public abstract class ToolProfile {

  public abstract Map<String,ToolProfileEntry> tools();

}