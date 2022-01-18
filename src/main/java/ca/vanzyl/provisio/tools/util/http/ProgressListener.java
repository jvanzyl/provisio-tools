package ca.vanzyl.provisio.tools.util.http;

public interface ProgressListener {
  void onProgressChanged(long readBytes, long totalBytes);
}