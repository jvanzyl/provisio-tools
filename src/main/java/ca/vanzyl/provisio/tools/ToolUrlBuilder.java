package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.Provisio.ARCH;
import static ca.vanzyl.provisio.tools.Provisio.OS;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ToolUrlBuilder {

  public static String interpolateToolPath(String toolPath, ToolDescriptor toolDescriptor, String version) {
    String toolVersion = version != null ? version : toolDescriptor.defaultVersion();
    String os = OS;
    String arch = ARCH;

    if(toolDescriptor.osMappings() != null) {
      if(toolDescriptor.osMappings().get(os) != null) {
        os = toolDescriptor.osMappings().get(os);
      }
    }

    if(toolDescriptor.archMappings() != null) {
      if(toolDescriptor.archMappings().get(arch) != null) {
        arch = toolDescriptor.archMappings().get(arch);
      }
    }

    return toolPath
        .replaceAll("\\{version\\}", toolVersion)
        .replaceAll("\\{os\\}", os)
        .replaceAll("\\{arch\\}",arch);
  }

  public static String buildUrlFor(ToolDescriptor toolDescriptor, String version) {
    return interpolateToolPath(toolDescriptor.downloadUrlTemplate(), toolDescriptor, version);
  }

  public static Path cachePathForTool(Path cacheDirectory, ToolDescriptor tool, String version) {
    // This takes the various different forms of paths based on where the tool has been deployed
    String url = buildUrlFor(tool, version);
    Path directory = cacheDirectory.resolve(Paths.get(tool.id(), version));
    // This is not always going to be a file name
    String file = url.substring(url.lastIndexOf('/') + 1);
    return directory.resolve(file);
  }

}
