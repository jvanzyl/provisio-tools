package ca.vanzyl.provisio.tools.generator.github;

import ca.vanzyl.provisio.tools.generator.ImmutableReleaseInfo;
import ca.vanzyl.provisio.tools.generator.ReleaseInfo;
import ca.vanzyl.provisio.tools.generator.ReleaseSource;
import java.util.stream.Collectors;

public class GitHubReleaseSource implements ReleaseSource {

  @Override
  public boolean canProcess(String url) {
    return url.startsWith("https://github.com") && url.endsWith("/releases");
  }

  @Override
  public ReleaseInfo info(String url) throws Exception {
    // https://github.com/pulumi/pulumi/releases
    // https://api.github.com/repos/pulumi/pulumi/releases
    System.out.println("We have a GitHub release ...");
    GitHubRelease latestRelease = new GitHubLatestReleaseFinder().find(url);
    return ImmutableReleaseInfo.builder()
        .name(latestRelease.repository()) // we'll make an assumption here the tool is named after the repo
        .urls(latestRelease.assets().stream().map(Asset::downloadUrl).collect(Collectors.toList()))
        .version(latestRelease.version())
        .build();
  }
}
