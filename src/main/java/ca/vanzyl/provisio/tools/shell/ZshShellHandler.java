package ca.vanzyl.provisio.tools.shell;

import ca.vanzyl.provisio.tools.model.ProvisioningRequest;
import java.nio.file.Path;

public class ZshShellHandler extends BashShellHandler {

  private final static String[] zshInitScripts = new String[]{
      ".zprofile",
      ".zshrc"
  };

  public ZshShellHandler(Path userHome, ProvisioningRequest request) {
    super(userHome, request);
  }

  public String[] shellInitScripts() {
    return zshInitScripts;
  }
}
