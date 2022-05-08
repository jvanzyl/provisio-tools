package ca.vanzyl.provisio.tools.util;

import static ca.vanzyl.provisio.tools.tool.ToolUrlBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.ProvisioTestSupport;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import org.junit.Test;

public class ToolUrlBuilderTest extends ProvisioTestSupport {

  @Test
  public void validateOsAndArchsubstitution() throws Exception {
    ToolDescriptor toolDescriptor = toolDescriptor("java");
    String url = toolDownloadUrlFor(toolDescriptor, "jdk-17.0.2+8", "Darwin", "arm64");
    assertThat(url).isEqualTo("https://api.adoptium.net/v3/binary/version/jdk-17.0.2+8/mac/aarch64/jdk/hotspot/normal/eclipse?project=jdk");
  }
}
