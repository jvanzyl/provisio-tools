package ca.vanzyl.provisio.tools;

import static kr.motd.maven.os.Detector.normalizeArch;
import static kr.motd.maven.os.Detector.normalizeOs;

public class ToolUrlBuilder {

  public static String build(ToolDescriptor toolDescriptor, String version) {
    String toolVersion = version != null ? version : toolDescriptor.defaultVersion();
    String architecture = normalizeArch(System.getProperty("os.arch"));
    String os = normalizeOs(System.getProperty("os.name"));
    return toolDescriptor.urlTemplate()
        .replaceAll("\\{version\\}", toolVersion)
        .replaceAll("\\{os\\}", os)
        .replaceAll("\\{arch\\}",architecture);
  }
}
