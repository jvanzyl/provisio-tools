package ca.vanzyl.provisio.tools.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Replace with Airlift
public class CliCommand {

  private final boolean saveOutput;
  private final boolean silent;
  private final Path workDir;
  private final List<String> args;
  private final Map<String, String> envars;

  public CliCommand(List<String> args, Path workDir, Map<String, String> envars, boolean saveOutput) {
    this(args, workDir, envars, saveOutput, false);
  }


  public CliCommand(List<String> args, Path workDir, Map<String, String> envars, boolean saveOutput, boolean silent) {
    this.workDir = workDir;
    this.args = args;
    this.envars = envars;
    this.saveOutput = saveOutput;
    this.silent = silent;
  }

  protected static void log(String line) {
    System.out.println(line);
  }

  public Result execute()
      throws Exception {
    return execute(Executors.newCachedThreadPool());
  }

  public Result execute(ExecutorService executor)
      throws Exception {
    ProcessBuilder pb = new ProcessBuilder(args).directory(workDir.toFile());
    Map<String, String> combinedEnv = new HashMap<>(envars);
    pb.environment().putAll(combinedEnv);
    Process p = pb.start();
    Future<String> stderr = executor.submit(new StreamReader(saveOutput, silent, p.getErrorStream()));
    Future<String> stdout = executor.submit(new StreamReader(saveOutput, silent, p.getInputStream()));
    int code = p.waitFor();
    executor.shutdown();
    return new Result(code, stdout.get(), stderr.get());
  }

  private static class StreamReader
      implements Callable<String> {

    private final boolean saveOutput;

    private final boolean silent;
    private final InputStream in;

    private StreamReader(boolean saveOutput, boolean silent, InputStream in) {
      this.saveOutput = saveOutput;
      this.silent = silent;
      this.in = in;
    }

    @Override
    public String call()
        throws Exception {
      StringBuilder sb = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (saveOutput) {
            sb.append(line).append(System.lineSeparator());
          }
          if(!silent) {
            log(line);
          }
        }
      }
      return sb.toString();
    }
  }

  public static class Result {

    private final int code;
    private final String stdout;
    private final String stderr;

    public Result(int code, String stdout, String stderr) {
      this.code = code;
      this.stdout = stdout;
      this.stderr = stderr;
    }

    public int getCode() {
      return code;
    }

    public String getStdout() {
      return stdout;
    }

    public String getStderr() {
      return stderr;
    }
  }
}