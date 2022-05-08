package ca.vanzyl.provisio.tools.model;

import static ca.vanzyl.provisio.tools.Provisio.OS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/*

id: helm
name: Helm
defaultVersion: 3.6.3
layout: file
executable: helm
packaging: TARGZ_STRIP
osMappings:
  Darwin: darwin
  Linux: linux
archMappings:
  x86_64: amd64
# https://get.helm.sh/helm-v3.6.3-linux-amd64.tar.gz
# https://get.helm.sh/helm-v3.6.3-darwin-amd64.tar.gz
urlTemplate: https://get.helm.sh/helm-v{version}-{os}-{arch}.tar.gz

 */

@Value.Immutable
@JsonDeserialize(as = ImmutableToolDescriptor.class)
public abstract class ToolDescriptor {

  public static final String DESCRIPTOR = "descriptor.yml";

  public abstract String id();

  public abstract String name();

  //TODO: this should not be nullable ultimately, but we started not collecting the source
  @Nullable
  public abstract List<String> sources();

  public abstract String defaultVersion();

  public abstract String layout();

  public abstract String executable();

  public abstract Packaging packaging();

  // For an installation which directories to add to the path
  @Nullable
  public abstract String paths();

  @Value.Default
  public boolean fileNameFromContentDisposition() {
    return false;
  }

  @Nullable
  public abstract String version();

  @Value.Derived
  @JsonIgnore
  public String downloadUrlTemplate() {
    String urlTemplate = urlTemplate();
    if(urlTemplate == null) {
      if(OS.equals("Darwin")) {
        urlTemplate = darwinUrlTemplate();
      } else if (OS.equals("Linux")) {
        urlTemplate = linuxUrlTemplate();
      } else {
        throw new RuntimeException("There is no download url template for " + id());
      }
    }
    return urlTemplate;
  }

  @Nullable
  public abstract Map<String,String> osMappings();

  @Nullable
  public abstract Map<String,String> archMappings();

  @Nullable
  public abstract String urlTemplate();

  @Nullable
  public abstract String linuxUrlTemplate();

  @Nullable
  public abstract String darwinUrlTemplate();

  @Nullable
  public abstract String windowsUrlTemplate();

  @Nullable
  public abstract String tarSingleFileToExtract();

  public enum Packaging {
    FILE,
    INSTALLER,
    TARGZ,
    TARGZ_STRIP,
    ZIP,
    ZIP_STRIP,
    ZIP_JUNK
  }
}