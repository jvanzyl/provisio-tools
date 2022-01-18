package ca.vanzyl.provisio.tools.util.http;

import static ca.vanzyl.provisio.tools.Provisio.IN_PROGRESS_EXTENSION;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.cachePathFor;
import static ca.vanzyl.provisio.tools.util.ToolUrlBuilder.toolDownloadUrlFor;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.move;

import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

// TODO: retry on Connection reset by peer
// TODO: use range header and resume
// TODO: signature/checksum valiation upon download
// TODO: need a reasonable timeout value

// https://golb.hplar.ch/2019/01/java-11-http-client.html
// https://www.baeldung.com/java-9-http-client

public class DownloadManager {

  private final Path cacheDirectory;

  public DownloadManager(Path cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
  }

  public Path resolve(ToolDescriptor tool, String version) throws Exception {
    // The url is constructed from the url template in the tool descriptor along with os, arch and version data.
    // For example, the url template for kubectl may look like this:
    //
    // https://dl.k8s.io/release/v{version}/bin/{os}/{arch}/kubectl
    //
    String url = toolDownloadUrlFor(tool, version);
    Path target;
    //
    // When downloading from endpoints that are APIs are other non-file endpoints, we may need to look at the
    // Content-Disposition header to determine what the intended file name is. Whereas sources like GitHub releases
    // make the file names available as it is part of the url.
    //
    if(tool.fileNameFromContentDisposition()) {
      String fileName = fileNameFromContentDisposition(url, tool);
      target = cachePathFor(cacheDirectory, tool, version, fileName);
    } else {
      target = cachePathFor(cacheDirectory, tool, version);
    }
    if (exists(target)) {
      return target;
    }
    createDirectories(target.getParent());
    //
    // When we download artifacts into their cache locations we do so with a file name that indicates that
    // the download is in progress. If the process is interrupted we can either remove the progress file and
    // start over or attempt to use a range head to resume the download.
    //
    Path inProgress = target.resolveSibling(target.getFileName() + IN_PROGRESS_EXTENSION);
    // Right now we will delete in progress files, we can improve later and resume
    deleteIfExists(inProgress);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(correctMalformedUrl(url, tool)))
        // client will fallback to http/1.1 if http/2 is not supported
        .version(HttpClient.Version.HTTP_2)
        .GET()
        .build();
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();
    HttpResponse<Path> response = client.send(request, BodyHandlers.ofFile(inProgress));
    if (response.statusCode() == 404) {
      throw new RuntimeException(String.format("The URL %s doesn't exist.", url));
    }

    // Now we attempt to atomically move our file into place
    move(inProgress, target, StandardCopyOption.ATOMIC_MOVE);

    return target;
  }

  private String fileNameFromContentDisposition(String url, ToolDescriptor tool) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(correctMalformedUrl(url, tool)))
        // client will fallback to http/1.1 if http/2 is not supported
        .version(HttpClient.Version.HTTP_2)
        .method("HEAD", HttpRequest.BodyPublishers.noBody())
        .build();
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();
    HttpResponse<Void> response = client.send(request, BodyHandlers.discarding());
    if (response.statusCode() == 404) {
      throw new RuntimeException(String.format("The URL %s doesn't exist.", url));
    }

    // Truly horrible, use streams
    List<String> headers = response.headers().allValues("Content-Disposition");
    for(String header : headers) {
      if(header.contains(";")) {
        String[] parts = header.split(";");
        for(String part : parts) {
          if(part.contains("filename=")) {
            return part.replace("filename=", "").trim();
          }
        }
      }
    }
    return null;
  }

  private String correctMalformedUrl(String url, ToolDescriptor tool) {
    String corrected = url.replace(" ", "");
    if(!corrected.equals(url)) {
      System.out.println("!!! '" + url + "' corrected to '" + corrected + "'");
      System.out.println("You'll want to look at the descriptor for " + tool.id() + " as the url template looks malformed.");
      System.out.println(tool.downloadUrlTemplate());
    }
    return corrected;
  }
}
