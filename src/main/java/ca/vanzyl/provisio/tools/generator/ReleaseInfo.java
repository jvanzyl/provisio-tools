package ca.vanzyl.provisio.tools.generator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableReleaseInfo.class)
public abstract class ReleaseInfo {

  public abstract String name();

  public abstract String version();

  public abstract List<String> urls();
}