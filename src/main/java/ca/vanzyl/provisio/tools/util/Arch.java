package ca.vanzyl.provisio.tools.util;

import kr.motd.maven.os.Detector;

public class Arch {

  public void execute()  {
    System.out.println(Detector.ARCH);
  }

  public static void main(String[] args) throws Exception {
    new Arch().execute();
  }
}
