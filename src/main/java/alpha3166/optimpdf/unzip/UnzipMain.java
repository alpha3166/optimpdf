package alpha3166.optimpdf.unzip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import alpha3166.optimpdf.framework.AbstractCommandMain;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "unzip", description = "Create PDF from images in ZIP.")
public class UnzipMain extends AbstractCommandMain {
  @Mixin
  UnzipOption opt;

  @Override
  protected void adjustFileMap(Map<Path, Path> fileMap) throws Exception {
    fileMap.replaceAll((k, v) -> Paths.get(v.toString().replaceFirst("(\\.\\w+)?$", ".pdf")));
  }

  @Override
  protected boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception {
    var zipHandler = new ZipHandler(src);
    var imagePathList = zipHandler.getImagePathList();
    PdfHandler.createPdfFromImages(temp, imagePathList, opt.rightToLeft);
    zipHandler.close();
    return true;
  }
}
