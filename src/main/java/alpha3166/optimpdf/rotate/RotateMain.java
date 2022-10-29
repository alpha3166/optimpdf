package alpha3166.optimpdf.rotate;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import alpha3166.optimpdf.framework.AbstractCommandMain;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "rotate", description = "Update PDF page rotation.")
public class RotateMain extends AbstractCommandMain {
  @Mixin
  RotateOption opt;

  @Override
  protected void handleLocalOption(Map<Path, Path> fileMap) throws Exception {
    if (opt.degree != 0 && opt.degree != 90 && opt.degree != 180 && opt.degree != 270) {
      throw new IllegalArgumentException("--degree must be 0, 90, 180, or 270");
    }

    if (opt.refPdf != null) {
      if (!Files.isRegularFile(opt.refPdf)) {
        throw new NoSuchFileException(opt.refPdf.toString());
      }
      for (var src : fileMap.keySet()) {
        if (opt.refPdf.toRealPath().equals(src.toRealPath())) {
          throw new IllegalArgumentException("Reference and target PDFs are the same");
        }
      }
    }
  }

  @Override
  protected boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception {
    var isRefPdfSpecified = opt.refPdf != null;
    var refRotation = new RefRotation(opt.refPdf, opt.degree);
    return PdfHandler.updatePdfPageRotation(src, temp, isRefPdfSpecified, refRotation);
  }
}
