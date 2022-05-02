package ca.vanzyl.provisio.tools.model;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.list;
import static java.nio.file.Files.readString;
import static java.nio.file.Paths.get;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableProvisioningRequest.class)
public abstract class ProvisioningRequest {

  public static final String PROVISIO_BIN = "bin";
  public static final String PROVISIO_CACHE = "cache";
  public static final String PROVISIO_INSTALLS = "installs";
  public static final String PROVISIO_PROFILES = "profiles";
  public static final String PROVISIO_PROFILE_YAML = "profile.yaml";
  public static final String TOOL_DESCRIPTOR = "descriptor.yml";


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
  public Path configDirectory() {
    return provisioRoot().resolve("config");
  }

  @Value.Default
  public Path configLastRevisionDirectory() {
    return provisioRoot().resolve("config.lastRevision");
  }

  @Value.Default
  public Path toolDescriptorsDirectory() {
    return configDirectory().resolve("tools");
  }

  @Value.Default
  public Path libexecDirectory() {
    return configDirectory().resolve("libexec");
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

  // User Profile

  @Value
  @Nullable
  public abstract String userProfile();

  @Value.Default
  public String activeUserProfile() {
    return userProfile() != null && !userProfile().equals("unset") ? userProfile() : findCurrentProfile();
  }

  @Value.Default
  public Path userProfileYaml() {
    return userProfilesDirectory().resolve(activeUserProfile()).resolve(PROVISIO_PROFILE_YAML);
  }

  @Value.Default
  public Path binaryProfileDirectory() {
    return binaryProfilesDirectory().resolve(activeUserProfile());
  }

  private String findCurrentProfile() {
    Path currentProfilePath = binaryProfilesDirectory().resolve("current");
    if(exists(currentProfilePath)) {
      try {
        return readString(binaryProfilesDirectory().resolve("current"));
      } catch(Exception e) {
        // let fall through
      }
    }
    Path currentProfileSymlink = binaryProfilesDirectory().resolve("profile");
    if(exists(currentProfileSymlink)) {
      return currentProfileSymlink.toString();
    }
    // TODO: there can only be one directory or we may select the wrong one
    try {
      Path profileDirectory = list(binaryProfilesDirectory()).filter(Files::isDirectory).findFirst().orElse(null);
      if (profileDirectory != null) {
        return profileDirectory.getFileName().toString();
      }
    } catch (Exception e) {
      // let fall through
    }
    throw new RuntimeException(
        format("The current profile cannot be determined. You should have file called " + currentProfilePath + "%n"
            + "with the name of the current profile or a symlink called " + currentProfileSymlink + " with a pointer %n"
            + "to the current profile. Run 'provisio install <profile>' to correct the issue."));
  }
}