package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.Provisio.*;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import java.util.Map;
import org.junit.Test;

public class ProvisioTest extends ProvisioTestSupport {

  @Test
  public void toolDescriptors() throws Exception {
    Map<String, ToolDescriptor> toolDescriptors = collectToolDescriptorsMap();
  }
}
