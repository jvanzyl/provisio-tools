package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptors;
import static ca.vanzyl.provisio.tools.ToolProvisioner.collectToolDescriptorsMap;

import org.junit.Test;

public class ToolDescriptorMapperTest {

  @Test
  public void validateToolDescriptorMappings() throws Exception {
    collectToolDescriptors()
        .forEach(System.out::println);
  }

  /*
  @Test
  public void validateToolUrlBuilding() throws Exception {
    collectToolDescriptorsMap().forEach((k, v) -> {
      System.out.println(k + ": ");
      System.out.println("  version: " + v.defaultVersion());
    });
  }

   */
}
