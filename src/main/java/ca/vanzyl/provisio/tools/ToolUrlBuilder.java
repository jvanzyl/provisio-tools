package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolProvisioner.ARCH;
import static ca.vanzyl.provisio.tools.ToolProvisioner.OS;

public class ToolUrlBuilder {

  public static String build(ToolDescriptor toolDescriptor, String version) {
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

    return toolDescriptor.downloadUrlTemplate()
        .replaceAll("\\{version\\}", toolVersion)
        .replaceAll("\\{os\\}", os)
        .replaceAll("\\{arch\\}",arch);
  }
}
