package ca.vanzyl.provisio.tools;

import ca.vanzyl.provisio.archive.generator.ArtifactEntry;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;

public class Generators {

  public static ArtifactEntry artifactEntry(String name, String content) {
    return new ArtifactEntry(name, content);
  }

  public static ToolProfileEntry toolProfileEntry(String toolId, String version) {
    return ImmutableToolProfileEntry.builder()
        .name(toolId)
        .version(version)
        .build();
  }
}
