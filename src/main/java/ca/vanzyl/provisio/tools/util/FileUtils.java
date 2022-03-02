package ca.vanzyl.provisio.tools.util;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class FileUtils {

  public static void deleteDirectoryIfExists(Path target) throws IOException {
    if (exists(target)) {
      deleteDirectory(target);
    }
  }

  public static void deleteDirectory(Path target) throws IOException {
    walk(target)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  public static void moveDirectoryIfExists(Path source, Path target) throws IOException {
    if (exists(source)) {
      copyFolder(source, target);
      deleteDirectory(source);
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

  public static void line(Path path, String line, Object... options) throws IOException {
    writeString(path, format(line, options), StandardOpenOption.APPEND);
  }

  public static void touch(Path path) throws IOException {
    createDirectories(path.getParent());
    // Without this line it fails in Graal, some some default modes must be different
    deleteIfExists(path);
    createFile(path);
  }

  public static void touch(Path path, String content) throws IOException {
    createDirectories(path.getParent());
    writeString(path, content);
  }
}
