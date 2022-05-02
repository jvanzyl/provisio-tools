package ca.vanzyl.provisio.tools.profile;

import ca.vanzyl.provisio.tools.ProvisioTestSupport;
import java.nio.file.Path;
import java.util.Map;
import org.junit.Test;

public class ProfileMapperTest extends ProvisioTestSupport {

  @Test(expected = RuntimeException.class)
  public void validateToolAdditionFailsWhenToolIsNotAvailable() throws Exception {
    Path profileYaml = testProfile("jvanzyl");
    ProfileMapper profileMapper = new ProfileMapper(profileYaml, Map.of());
    profileMapper.add("foo", "1.0");
  }
}
