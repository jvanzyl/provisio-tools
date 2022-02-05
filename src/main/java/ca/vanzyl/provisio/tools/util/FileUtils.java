package ca.vanzyl.provisio.tools.util;

import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class FileUtils {

  public static void deleteDirectoryIfExists(Path target) throws IOException {
    if(exists(target)) {
      walk(target)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  public static void resetDirectory(Path target) throws IOException {
    deleteDirectoryIfExists(target);
    createDirectories(target);
  }

  public static void copyFolder(Path src, Path dest) throws IOException {
    try (Stream<Path> stream = walk(src)) {
      stream.filter(Files::isRegularFile)
          .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }
  }

  private static void copy(Path source, Path dest) {
    try {
      Files.createDirectories(dest.getParent());
      Files.copy(source, dest, REPLACE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static void makeExecutable(Path path) throws IOException {
    final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
    permissions.add(PosixFilePermission.OWNER_EXECUTE);
    Files.setPosixFilePermissions(path, permissions);
  }

  public static void updateSymlink(Path link, Path target) throws IOException {
    deleteIfExists(link);
    createSymbolicLink(link, target);
  }

  public static void updateRelativeSymlink(Path link, Path target) throws IOException {
    deleteIfExists(link);
    createSymbolicLink(link, link.getParent().relativize(target));
  }

  public static void writeFile(Path file, String content) throws IOException {
    createDirectories(file.getParent());
    writeString(file, content);
  }
}
