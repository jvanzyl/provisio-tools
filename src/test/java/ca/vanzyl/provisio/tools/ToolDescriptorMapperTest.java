package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptors;
import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptorsMap;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.build;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.tools.Tool;
import org.junit.Test;

public class ToolDescriptorMapperTest {

  @Test
  public void validateToolDescriptorMappings() throws Exception {
    collectToolDescriptors()
        .forEach(System.out::println);
  }

  @Test
  public void validateToolUrlBuilding() throws Exception {
    Map<String, ToolDescriptor> toolDescriptorsById = collectToolDescriptorsMap();

    YamlMapper<ToolUrlTestDescriptor> mapper = new YamlMapper<>();
    List<ToolUrlTestDescriptor> tools = mapper.read(Paths.get("/Users/jvanzyl/.provisio/tool-urls.yaml"), new TypeReference<>() {});
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
