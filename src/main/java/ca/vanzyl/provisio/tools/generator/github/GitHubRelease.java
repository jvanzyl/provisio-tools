package ca.vanzyl.provisio.tools.generator.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableGitHubRelease.class)
public abstract class GitHubRelease {

  @Nullable
  public abstract String repository();

  @Nullable
  public abstract String organization();

  @JsonProperty("tag_name")
  public abstract String version();

  public abstract List<Asset> assets();

}