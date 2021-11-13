package ca.vanzyl.provisio.tools;

import java.nio.file.Paths;
import org.junit.Test;

public class DownloadManagerTest {

  @Test
  public void validateDownloadManagerCanRetrieveFiles() throws Exception {

    DownloadManager downloadManager = new DownloadManager(Paths.get("/tmp/target"));
    downloadManager.resolve("https://repo1.maven.org/maven2/io/takari/polyglot/maven-metadata.xml");

  }
}
