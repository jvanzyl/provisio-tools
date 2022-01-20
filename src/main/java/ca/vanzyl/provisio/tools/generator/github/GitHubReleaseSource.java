package ca.vanzyl.provisio.tools.generator.github;

import static java.lang.String.format;

import ca.vanzyl.provisio.tools.generator.ImmutableReleaseInfo;
import ca.vanzyl.provisio.tools.generator.ReleaseInfo;
import ca.vanzyl.provisio.tools.generator.ReleaseSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
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
    String[] s = url.substring("https://".length()).split("/");
    String organization = s[1];
    String repository = s[2];
    String api = format("https://api.github.com/repos/%s/%s/releases", organization, repository);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(api))
        .version(HttpClient.Version.HTTP_2)
        .GET()
        .build();
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();
    HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

    // https://stackoverflow.com/questions/57629401/deserializing-json-using-java-11-httpclient-and-custom-bodyhandler-with-jackson
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    List<GitHubRelease> releases = mapper.readValue(response.body(), new TypeReference<>() {});
    GitHubRelease latestRelease = releases.get(0);

    return ImmutableReleaseInfo.builder()
        .name(repository) // we'll make an assumption here the tool is named after the repo
        .urls(latestRelease.assets().stream().map(Asset::downloadUrl).collect(Collectors.toList()))
        .version(latestRelease.version())
        .build();
  }
}
