package ca.vanzyl.provisio.tools.generator.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableAsset.class)
public abstract class Asset {

  // File name
  @JsonProperty("name")
  public abstract String fileName();

  @JsonProperty("browser_download_url")
  public abstract String downloadUrl();


}