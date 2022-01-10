package ca.vanzyl.provisio.tools.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileUtils {

  public static void deleteDirectory(Path target) throws IOException {
    if(Files.exists(target)) {
      Files.walk(target)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  public static void resetDirectory(Path target) throws IOException {
    deleteDirectory(target);
    Files.createDirectories(target);
  }
}
