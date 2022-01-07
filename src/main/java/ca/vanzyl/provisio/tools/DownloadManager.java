package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.ToolUrlBuilder.buildUrlFor;
import static ca.vanzyl.provisio.tools.ToolUrlBuilder.cachePathForTool;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProfileEntry;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadManager {

  private final Path cacheDirectory;

  public DownloadManager(Path cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
  }

  public Path resolve(ToolDescriptor tool, String version) throws Exception {
    String url = buildUrlFor(tool, version);
    Path target = cachePathForTool(cacheDirectory, tool, version);
    if (exists(target)) {
      return target;
    }
    createDirectories(target.getParent());
    // https://www.baeldung.com/java-9-http-client
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(url))
        // client will fallback to http/1.1 if http/2 is not supported
        .version(HttpClient.Version.HTTP_2)
        .GET()
        .build();
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();
    HttpResponse<Path> response = client.send(request, BodyHandlers.ofFile(target));
    if (response.statusCode() == 404) {
      throw new RuntimeException(String.format("The URL %s doesn't exist.", url));
    }
    return target;
  }
}
