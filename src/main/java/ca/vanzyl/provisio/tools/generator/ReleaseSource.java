package ca.vanzyl.provisio.tools.generator;

public interface ReleaseSource {
  boolean canProcess(String url);
  ReleaseInfo info(String url) throws Exception;
}
