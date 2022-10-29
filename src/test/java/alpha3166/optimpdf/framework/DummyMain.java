package alpha3166.optimpdf.framework;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "dummy")
public class DummyMain extends AbstractCommandMain {
  @Option(names = "--ext")
  String ext;

  @Option(names = "--process-result", arity = "1")
  boolean processResult = true;

  @Override
  protected void adjustFileMap(Map<Path, Path> fileMap) throws Exception {
    fileMap.keySet().removeIf(k -> !k.toString().endsWith(".txt"));
  }

  @Override
  protected void handleLocalOption(Map<Path, Path> fileMap) throws Exception {
    if (ext != null) {
      fileMap.replaceAll((k, v) -> Paths.get(v.toString().replaceFirst("(\\.\\w+)?$", "." + ext)));
    }
  }

  @Override
  protected boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception {
    Files.writeString(temp, "done");
    return processResult;
  }
}
