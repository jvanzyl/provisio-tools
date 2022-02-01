package ca.vanzyl.provisio.tools.model;

import static java.nio.file.Paths.get;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableProvisioningRequest.class)
public abstract class ProvisioningRequest {

  public static final String PROVISIO_BIN = "bin";
  public static final String PROVISIO_CACHE = "cache";
  public static final String PROVISIO_INSTALLS = "installs";
  public static final String PROVISIO_PROFILES = "profiles";

  @Value.Default
  public Path provisioRoot() {
    return get(System.getProperty("user.home"), ".provisio");
  }

  // Configuration: where the users yaml profiles live that describe the version of the tools they want provisioned

  @Value.Default
  public Path userProfilesDirectory() {
    return provisioRoot().resolve("profiles");
  }

  @Value.Default
  public Path workingDirectoryProfilesDirectory() {
    return get(System.getProperty("user.dir")).resolve(".provisio").resolve("profiles");
  }

  @Value.Default
  public Path toolDescriptorsDirectory() {
    return provisioRoot().resolve("tools");
  }

  // Binary: where all the installations, cache and binary instances of profiles

  @Value.Default
  public Path binDirectory() {
    return provisioRoot().resolve(PROVISIO_BIN);
  }

  @Value.Default
  public Path installsDirectory() {
    return binDirectory().resolve("installs");
  }

  @Value.Default
  public Path cacheDirectory() {
    return binDirectory().resolve("cache");
  }

  @Value.Default
  public Path binaryProfilesDirectory() {
    return binDirectory().resolve("profiles");
  }
}