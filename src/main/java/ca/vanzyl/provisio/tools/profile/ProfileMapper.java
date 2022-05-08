package ca.vanzyl.provisio.tools.profile;

import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import ca.vanzyl.provisio.tools.model.ImmutableToolProfile;
import ca.vanzyl.provisio.tools.model.ImmutableToolProfileEntry;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import ca.vanzyl.provisio.tools.util.YamlMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileMapper {

  private final Path profileYaml;
  private final YamlMapper<ToolProfile> profileMapper;
  private final Map<String, ToolDescriptor> tools;

  public ProfileMapper(Path profileYaml, Map<String, ToolDescriptor> toolDescriptorMap) {
    this.profileYaml = profileYaml;
    this.tools = toolDescriptorMap;
    this.profileMapper = new YamlMapper<>();
  }

  public ToolProfile read() throws IOException {
    return profileMapper.read(profileYaml, ToolProfile.class);
  }

  public void add(String toolAtVersion) throws IOException {
    String toolId;
    String version;
    if(toolAtVersion.indexOf('@') == -1) {
      toolId = toolAtVersion;
      version = "default";
    } else {
      String[] s = toolAtVersion.split("@");
      toolId = s[0];
      version = s[1];
    }
    add(toolId, version);
  }

  public void add(String toolId, String version) throws IOException {
    ToolDescriptor toolDescriptor = tools.get(toolId);
    if(toolDescriptor == null) {
      throw new RuntimeException(format("The tool %s is not among the available tools to add.", toolId));
    }
    ToolProfile profile = read();
    ToolProfileEntry toolEntry = ImmutableToolProfileEntry.builder()
        .name(toolId)
        .version(version.equals("default") ? toolDescriptor.defaultVersion() : version)
        .build();
    Map<String, ToolProfileEntry> tools = new LinkedHashMap<>(profile.tools());
    tools.put(toolId, toolEntry);
    // This will place the tool at the end of the profile as the implementation is LinkedHashMap
    ToolProfile newProfile = ImmutableToolProfile.builder().from(profile).tools(tools).build();
    String profileWithAddedTool = profileMapper.write(newProfile);
    Path profileYamlLastRevision = profileYaml.resolveSibling("profile.yaml.lastRevision");
    move(profileYaml, profileYamlLastRevision, REPLACE_EXISTING);
    writeString(profileYaml, profileWithAddedTool);
  }
}
