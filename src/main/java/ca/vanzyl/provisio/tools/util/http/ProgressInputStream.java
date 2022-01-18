package ca.vanzyl.provisio.tools.util.http;

import java.io.IOException;
import java.io.InputStream;

class ProgressInputStream extends InputStream {
  private final InputStream stream;
  private final ProgressListener listener;

  private long totalBytes;
  private long readBytes;
  private int progress;

  ProgressInputStream(InputStream stream, ProgressListener listener, long totalBytes) {
    this.stream = stream;
    this.listener = listener;
    this.totalBytes = totalBytes;
  }

  public long totalBytes() {
    return totalBytes;
  }

  public void totalBytes(long total) {
    this.totalBytes = total;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public int read() throws IOException {
    int read = stream.read();
    readBytes++;
    listener.onProgressChanged(readBytes, totalBytes);
    return read;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int read = stream.read(b, off, len);
    readBytes += read;
    listener.onProgressChanged(readBytes, totalBytes);
    return read;
  }
}