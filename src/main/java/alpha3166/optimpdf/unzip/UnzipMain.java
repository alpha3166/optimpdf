package alpha3166.optimpdf.unzip;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "unzip", description = "Create PDF from images in the Zip")
public class UnzipMain implements Callable<Integer> {
  Logger logger = LoggerFactory.getLogger(getClass());

  @Mixin
  OptionParser arg;

  @Override
  public Integer call() throws Exception {
    var opt = new OptionHandler(arg);
    var zipMap = opt.zipMap();
    for (var zipPath : zipMap.keySet()) {
      logger.info(zipPath.toString());
      var pdfPath = zipMap.get(zipPath);
      var zipHandler = new ZipHandler(zipPath);
      var imagePathList = zipHandler.getImagePathList();
      PdfHandler.createPdfFromImages(pdfPath, imagePathList, opt.isRightToLeft());
      zipHandler.close();
    }
    return 0;
  }
}
