package ca.vanzyl.provisio.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class ProvisioTest {

  @Test
  public void validateToolProvisioning() throws Exception {
    // Need to separate between the cache for downloading and target directory
    Path target = Paths.get("target", "tool");
    Provisio provisio = new Provisio(target);
    provisio.provisionTool(target, "yq");
  }
}
