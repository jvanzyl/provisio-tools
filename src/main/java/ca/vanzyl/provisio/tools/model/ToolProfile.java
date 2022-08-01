package ca.vanzyl.provisio.tools.model;

import static kr.motd.maven.os.Detector.ARCH;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolProfile.class)
public abstract class ToolProfile {

  @Nullable
  public abstract String arch();

  @Value.Derived
  public String derivedArch() {
    String arch = arch();
    if(arch != null) {
      return arch;
    }
    return ARCH;
  }

  @JsonDeserialize(contentUsing = MapKeyDeserializer.class)
  public abstract Map<String,ToolProfileEntry> tools();

}