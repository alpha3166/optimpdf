package alpha3166.optimpdf.framework;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class DataManager {
  public static Path makeTestDir() throws IOException {
    return Files.createTempDirectory(Paths.get(""), "junit");
  }

  public static void removeDir(Path dir) throws IOException {
    Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
