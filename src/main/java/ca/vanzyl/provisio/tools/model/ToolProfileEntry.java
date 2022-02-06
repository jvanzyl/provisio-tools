package ca.vanzyl.provisio.tools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolProfileEntry.class)
public abstract class ToolProfileEntry {

  @Nullable
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  public abstract String name();

  public abstract String version();

  @Nullable
  public abstract String pathManagedBy();

  @Nullable
  public abstract List<String> plugins();

  @Override
  public String toString() {
    // graalvm[21.3.0] pathManagedBy(jenv)
    // jenv[master] plugins[export, maven]
    String pathManagedBy = pathManagedBy() != null ? " pathManagedBy(" + pathManagedBy() + ") " : "";
    String plugins = plugins() != null ? " plugins" + plugins() + "" : "";
    return name() + "[" + version() + "]" + pathManagedBy + plugins;
  }
}