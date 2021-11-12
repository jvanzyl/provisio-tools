package ca.vanzyl.provisio.tools;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

  public abstract String executable();

  public abstract Packaging packaging();

  @Nullable
  public abstract String version();

  @Nullable
  public abstract String urlTemplate();

  @Nullable
  public abstract String linuxUrlTemplate();

  @Nullable
  public abstract String darwinUrlTemplate();

  @Nullable
  public abstract String windowsUrlTemplate();

  public abstract String defaultVersion();

  public enum Packaging {
    FILE,
    GIT,
    INSTALLER,
    TARGZ,
    TARGZ_STRIP,
    ZIP
  }
}