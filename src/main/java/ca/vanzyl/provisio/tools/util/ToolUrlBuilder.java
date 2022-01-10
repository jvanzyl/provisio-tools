package ca.vanzyl.provisio.tools.util;

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

  public static String toolDownloadUrlFor(ToolDescriptor toolDescriptor, String version) {
    return interpolateToolPath(toolDescriptor.downloadUrlTemplate(), toolDescriptor, version);
  }

  public static Path cachePathFor(Path cacheDirectory, ToolDescriptor tool, String version) {
    String url = toolDownloadUrlFor(tool, version);
    String file = url.substring(url.lastIndexOf('/') + 1);
    return cachePathFor(cacheDirectory, tool, version, file);
  }

  public static Path cachePathFor(Path cacheDirectory, ToolDescriptor tool, String version, String fileName) {
    return cacheDirectory.resolve(Paths.get(tool.id(), version)).resolve(fileName);
  }

}
