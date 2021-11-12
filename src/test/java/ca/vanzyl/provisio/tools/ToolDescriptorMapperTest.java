package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolProvisioner.PROVISIO_ROOT;
import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptors;
import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptorsMap;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.build;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ToolDescriptorMapperTest {

  protected final static Path testResources = PROVISIO_ROOT.resolve("target/test-resources");
  protected final static String testToolUrlsFile = "tool-urls.yaml";

  @Test
  public void validateToolDescriptorMappings() throws Exception {
    collectToolDescriptors()
        .forEach(System.out::println);
  }

  //
  // This validates that the way download URLs are built in the same way in the BASH version
  // as they are in the Java version. Make sure that you run `provisio test` first to generate
  // the testing resources used to test compatibility.
  //
  @Test
  public void validateToolUrlBuilding() throws Exception {
    Map<String, ToolDescriptor> toolDescriptorsById = collectToolDescriptorsMap();
    YamlMapper<ToolUrlTestDescriptor> mapper = new YamlMapper<>();
    List<ToolUrlTestDescriptor> tools = mapper.read(testResources.resolve(testToolUrlsFile), new TypeReference<>() {});
    tools.forEach(t -> {
      String id = t.id();
      String version = t.version();
      String expectedUrl = t.url();
      ToolDescriptor td = toolDescriptorsById.get(id);
      String actualUrl = build(td, version);
      assertThat(actualUrl).isEqualTo(expectedUrl);
    });
  }
}
