package ca.vanzyl.provisio.tools.profile;

import ca.vanzyl.provisio.tools.ProvisioTestSupport;
import java.nio.file.Path;
import java.util.Map;
import org.junit.Test;

// TODO: Make a profile generator for tests. We may always have a need
// TODO: Use tests that allow checking the error message

public class ProfileMapperTest extends ProvisioTestSupport {

  @Test(expected = RuntimeException.class)
  public void validateToolAdditionFailsWhenToolIsNotAvailable() throws Exception {
    Path profileYaml = testProfile("jvanzyl");
    ProfileMapper profileMapper = new ProfileMapper(profileYaml, Map.of());
    profileMapper.add("non-existent-tool", "1.0");
  }

  @Test(expected = RuntimeException.class)
  public void validateToolAdditionFailsWhenToolIsNotSpecifiedCorrectly() throws Exception {
    Path profileYaml = testProfile("jvanzyl");
    ProfileMapper profileMapper = new ProfileMapper(profileYaml, Map.of());
    profileMapper.add("tool-specification-with-no-at-symbol");
  }
}
