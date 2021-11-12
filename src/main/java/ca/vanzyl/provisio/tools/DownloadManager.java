package ca.vanzyl.provisio.tools;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public interface DownloadManager {
  Path resolve(URI var1) throws IOException;
}
