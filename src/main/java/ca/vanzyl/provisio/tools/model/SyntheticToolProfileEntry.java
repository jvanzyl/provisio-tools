package ca.vanzyl.provisio.tools.model;

import ca.vanzyl.provisio.archive.generator.ArtifactEntry;
import ca.vanzyl.provisio.tools.model.ToolDescriptor.Packaging;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableSyntheticToolProfileEntry.class)
public abstract class SyntheticToolProfileEntry extends ToolProfileEntry {

  public abstract Packaging packaging();

  public abstract String layout();

  @Value.Derived
  public String extension() {
    if(packaging().equals(Packaging.TARGZ) || packaging().equals(Packaging.TARGZ_STRIP)) {
      return "tar.gz";
    } else {
      return "zip";
    }
  }

  public abstract List<ArtifactEntry> artifactEntries();

}