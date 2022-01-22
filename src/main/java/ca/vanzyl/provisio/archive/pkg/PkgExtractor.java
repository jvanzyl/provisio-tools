package ca.vanzyl.provisio.archive.pkg;

import com.sprylab.xar.FileXarSource;
import com.sprylab.xar.XarEntry;
import com.sprylab.xar.XarSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;

// I probably need to parse the BOM and have more sample PKG files to test with
// It might just be easier to repackage the few things needed
// https://en.wikipedia.org/wiki/BOM_(file_format)
// https://github.com/gino0631/pkg
// The BOM file contains symlink and permission information but really what for if the CPIO archive has most of it
// it contains scripts to run after as well.
// XAR <-- GZ <-- CPIO

// Scripts is a gzipped CPIO archive as well

// In the tree test files there is only one payload directory
public class PkgExtractor {

  private boolean extract(File pkgFile, Path outputDir) throws IOException {
    XarSource xarFile = new FileXarSource(pkgFile);
    for (XarEntry xarEntry : xarFile.getEntries()) {
      String xarEntryName = xarEntry.getName();
      if (xarEntryName.endsWith("Payload")) {
        try (CpioArchiveInputStream inputStream = new CpioArchiveInputStream(new GZIPInputStream(xarEntry.getInputStream()))) {
          CpioArchiveEntry entry;
          while ((entry = (CpioArchiveEntry) inputStream.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
              Path path = outputDir.resolve(entry.getName()).toAbsolutePath();
              Files.createDirectories(path.getParent());
              if (entry.isRegularFile()) {
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                  inputStream.transferTo(outputStream);
                }
                // Only set permissions on real files
                Files.setPosixFilePermissions(path, PosixModes.intModeToPosix((int) entry.getMode() & 0000777));
              } else if (entry.isSymbolicLink()) {
                // The cpio command does not follow symbolic links, but instead saves the link text in the archive.
                Path target = Paths.get(new String(inputStream.readAllBytes()));
                Files.createSymbolicLink(path, target);
              }
            }
          }
        }
      }
    }
    return true;
  }

  public static void main(String[] args) throws Exception {
    File pkg = new File("/Users/jvanzyl/.provisio/.bin/.cache/aws-cli/2.2.14/AWSCLIV2-2.2.14.pkg");
    new PkgExtractor().extract(pkg, Paths.get("/tmp/provisio/0"));

    /*
    pkg = new File("/Users/jvanzyl/downloads/UniFi.pkg");
    new PkgExtractor().extract(pkg, Paths.get("/tmp/provisio/1"));

    pkg = new File("/Users/jvanzyl/downloads/OpenJDK17U-jdk_x64_mac_hotspot_17.0.1_12.pkg");
    new PkgExtractor().extract(pkg, Paths.get("/tmp/provisio/2"));
     */
  }
}
