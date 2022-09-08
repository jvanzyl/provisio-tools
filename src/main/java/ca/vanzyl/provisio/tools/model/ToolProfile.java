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
    // Specifically look for the docker buildx envar ${TARGETARCH} as it's the only way to
    // detect what platform you want to build for in an emulated environment.
    String arch = arch() != null ? arch() : System.getenv("TARGETARCH");
    if(arch != null) {
      // In the case of building multiplatform images with docker buildx under QEMU emulation we cannot
      // actually detect the architecture, but the ${TARGETARCH} envar is available but for x86_64
      // the value is amd64 so swap it for x86_64 as we use the output of uname -m as our template.
      if(arch.equals("amd64")) {
        arch = "x86_64";
      }
      return arch;
    }
    return ARCH;
  }

  @JsonDeserialize(contentUsing = MapKeyDeserializer.class)
  public abstract Map<String,ToolProfileEntry> tools();

}