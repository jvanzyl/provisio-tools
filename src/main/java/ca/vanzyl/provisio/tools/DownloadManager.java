package ca.vanzyl.provisio.tools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadManager {

  private final Path targetDirectory;

  public DownloadManager(Path targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public Path resolve(String url) throws Exception {
    String file = url.substring(url.lastIndexOf('/') + 1);
    Path target = targetDirectory.resolve(file);
    Files.createDirectories(target.getParent());
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
