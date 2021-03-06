package ca.vanzyl.provisio.tools.generator.github;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Paths.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class GitHubLatestReleaseFinder {

  public GitHubRelease find(String url) throws Exception {

    // https://github.com/pulumi/pulumi/releases
    // https://api.github.com/repos/pulumi/pulumi/releases

    String[] s = url.substring("https://".length()).split("/");
    String organization = s[1];
    String repository = s[2];
    String api = format("https://api.github.com/repos/%s/%s/releases", organization, repository);

    // TODO: start of support for tools in private repositories. But this needs to be generalized so that the downlooading
    // of tools works from private repos as well.
    Builder requestBuilder = HttpRequest.newBuilder();
    String user = System.getProperty("user.name");
    Path gitHubTokenFile = get(format("/Users/%s/.github/%s", user, user));
    if (exists(gitHubTokenFile)) {
      String gitHubToken = readString(gitHubTokenFile).trim();
      requestBuilder.header("Authorization", basicAuth(user, gitHubToken));
    }
    HttpRequest request = requestBuilder
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
    List<GitHubRelease> releases = mapper.readValue(response.body(), new TypeReference<>() {
    });
    return ImmutableGitHubRelease.copyOf(releases.get(0))
        .withOrganization(organization)
        .withRepository(repository);
  }

  private static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  }
}
