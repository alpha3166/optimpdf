package alpha3166.optimpdf.unzip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class OptionHandler {
  private Map<Path, Path> zipMap;
  private boolean rightToLeft;

  public OptionHandler(OptionParser arg) throws IOException {
    // Handle arguments
    zipMap = new TreeMap<>();
    for (var zipPath : arg.paths) {
      if (!Files.isRegularFile(zipPath)) {
        throw new NoSuchFileException(zipPath.toString());
      }
      var pdfPath = Paths.get(zipPath.toString().replaceFirst("(\\.\\w+)?$", ".pdf"));
      zipMap.put(zipPath, pdfPath);
    }

    // Handle -d
    if (arg.outDir != null) {
      if (!Files.isDirectory(arg.outDir)) {
        throw new NoSuchFileException(arg.outDir.toString());
      }
      zipMap.replaceAll((k, v) -> arg.outDir.resolve(v.getFileName()));
    }

    // Handle -r
    rightToLeft = arg.rightToLeft;
  }

  public Map<Path, Path> zipMap() {
    return zipMap;
  }

  public boolean isRightToLeft() {
    return rightToLeft;
  }
}
