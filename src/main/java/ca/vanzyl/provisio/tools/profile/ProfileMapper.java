package ca.vanzyl.provisio.tools.profile;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.move;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;

import ca.vanzyl.provisio.tools.Provisio;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfile;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileMapper {

  private final Path profileYaml;
  private final YamlMapper<ToolProfile> profileMapper;
  private final Map<String, ToolDescriptor> toolDescriptorMap;

  public ProfileMapper(Path profileYaml, Map<String, ToolDescriptor> toolDescriptorMap) {
    this.profileYaml = profileYaml;
    this.toolDescriptorMap = toolDescriptorMap;
    this.profileMapper = new YamlMapper<>();
  }

  public ToolProfile read() throws IOException {
    return profileMapper.read(profileYaml, ToolProfile.class);
  }

  public void add(String toolAtVersion) throws IOException {
    if(toolAtVersion.indexOf('@') == -1) {
      throw new RuntimeException("You must specify adding a new tool in the form <tool>@<version>");
    }
    String[] toolVersion = toolAtVersion.split("@");
    add(toolVersion[0], toolVersion[1]);
  }

  public void add(String toolId, String version) throws IOException {
    ToolProfile profile = read();
    ToolProfileEntry toolEntry = ImmutableToolProfileEntry.builder()
        .name(toolId)
        .version(version)
        .build();
    Map<String, ToolProfileEntry> tools = new LinkedHashMap<>(profile.tools());
    // Validate the tool exists before adding to the profile
    if(!toolDescriptorMap.containsKey(toolId)) {
      throw new RuntimeException(format("The tool %s is not among the available tools to add.", toolId));
    }
    tools.put(toolId, toolEntry);
    // This will place the tool at the end of the profile as the implementation is LinkedHashMap
    ToolProfile newProfile = ImmutableToolProfile.builder().from(profile).tools(tools).build();
    String profileWithAddedTool = profileMapper.write(newProfile);
    Path profileYamlLastRevision = profileYaml.resolveSibling("profile.yaml.lastRevision");
    move(profileYaml, profileYamlLastRevision);
    writeString(profileYaml, profileWithAddedTool);
  }
}
