package ca.vanzyl.provisio.archive.pkg;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public final class PosixModes {

  static final PosixFilePermission[] PERMISSIONS = PosixFilePermission.values();

  private static final int PERMISSIONS_LENGTH = PERMISSIONS.length;
  private static final int INT_MODE_MAX = (1 << PERMISSIONS_LENGTH) - 1;

  private PosixModes() {
    throw new Error("nice try!");
  }

  public static Set<PosixFilePermission> intModeToPosix(int intMode) {
    if ((intMode & INT_MODE_MAX) != intMode) {
      throw new RuntimeException("Invalid intMode: " + intMode);
    }
    final Set<PosixFilePermission> set = EnumSet.noneOf(PosixFilePermission.class);

    for (int i = 0; i < PERMISSIONS_LENGTH; i++) {
      if ((intMode & 1) == 1) {
        set.add(PERMISSIONS[PERMISSIONS_LENGTH - i - 1]);
      }
      /*
       * We're OK with >> instead of >>>, the sign bit will never be set
       */
      intMode >>= 1;
    }
    return set;
  }

  public static int posixToIntMode(Set<PosixFilePermission> perms) {
    int mask = 0;
    for (PosixFilePermission perm : perms) {
      mask |= 1 << (PERMISSIONS_LENGTH - perm.ordinal() - 1);
    }
    return mask;
  }
}
