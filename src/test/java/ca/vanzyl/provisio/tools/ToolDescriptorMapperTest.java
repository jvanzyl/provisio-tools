package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.Provisio.PROVISIO_ROOT;
import static ca.vanzyl.provisio.tools.Provisio.collectToolDescriptorsMap;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.toolDownloadUrlFor;
import static org.assertj.core.api.Assertions.assertThat;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProfile;
import ca.vanzyl.provisio.tools.model.ToolUrlTestDescriptor;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;

public class ToolDescriptorMapperTest {

  protected final static Path testResources = PROVISIO_ROOT.resolve("target/test-resources");
  protected final static String testToolUrls = "test-tool-urls.yaml";
  protected final static String testProfile = "test-profile.yaml";

  //
  // This validates that the way download URLs are built in the same way in the BASH version
  // as they are in the Java version. Make sure that you run `provisio test` first to generate
  // the testing resources used to test compatibility.
  //
  @Test
  @Ignore
  public void validateToolUrlBuilding() throws Exception {
    Map<String, ToolDescriptor> toolDescriptorsById = collectToolDescriptorsMap();
    YamlMapper<ToolUrlTestDescriptor> mapper = new YamlMapper<>();
    List<ToolUrlTestDescriptor> tools = mapper.read(testResources.resolve(testToolUrls), new TypeReference<>() {});
    tools.forEach(tool -> {
      String id = tool.id();
      String expectedUrl = tool.url();
      ToolDescriptor td = toolDescriptorsById.get(id);
      String actualUrl = toolDownloadUrlFor(td, tool.version());
      System.out.println("Validating " + id);
      assertThat(actualUrl).isEqualTo(expectedUrl);
    });
  }

  @Test
  @Ignore
  public void validateToolProfileMapping() throws Exception {
    YamlMapper<ToolProfile> mapper = new YamlMapper<>();
    ToolProfile profile = mapper.read(testResources.resolve(testProfile), ToolProfile.class);
    System.out.println(profile);
  }
}
