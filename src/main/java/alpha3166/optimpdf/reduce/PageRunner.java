package alpha3166.optimpdf.reduce;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageRunner implements Runnable {
  Logger logger = LoggerFactory.getLogger(getClass());

  private PdfHandler pdfHandler;
  private int page;
  private int screenWidth;
  private int screenHeight;
  private int doublePageThreshold;
  private int jpegQuality;
  private boolean bleach;
  private boolean quiet;

  public PageRunner(PdfHandler pdfHandler, int page, int screenWidth, int screenHeight, int doublePageThreshold,
      int jpegQuality, boolean bleach, boolean quiet) {
    this.pdfHandler = pdfHandler;
    this.page = page;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.doublePageThreshold = doublePageThreshold;
    this.jpegQuality = jpegQuality;
    this.bleach = bleach;
    this.quiet = quiet;
  }

  @Override
  public void run() {
    try {
      var log = new StringBuilder();
      log.append(String.format("  %d/%d", page, pdfHandler.getNumberOfPages()));

      var jpeg = pdfHandler.extractJpeg(page);
      var jpegHandler = new JpegHandler(jpeg);
      var width = jpegHandler.getWidth();
      var height = jpegHandler.getHeight();
      log.append(" " + jpegHandler.desc());

      var maxWidth = 0;
      var maxHeight = 0;
      if (width > doublePageThreshold || height > doublePageThreshold) {
        if (width < height) {
          maxWidth = screenWidth;
          maxHeight = screenHeight;
        } else {
          maxWidth = screenHeight;
          maxHeight = screenWidth;
        }
      } else {
        if (width < height) {
          maxWidth = screenHeight / 2;
          maxHeight = screenWidth;
        } else {
          maxWidth = screenWidth;
          maxHeight = screenHeight / 2;
        }
      }
      log.append(String.format(" (fit %dx%d%s)", maxWidth, maxHeight, bleach ? " bleach" : ""));

      var newJpegHandler = jpegHandler.resize(jpegQuality, maxWidth, maxHeight, bleach);
      var newJpegWidth = newJpegHandler.getWidth();
      var newJpegHeight = newJpegHandler.getHeight();
      var newJpegBytes = newJpegHandler.getBytes();
      var newJpegIsGray = newJpegHandler.isGray();
      log.append(String.format(" > %s %d%%", newJpegHandler.desc(), newJpegBytes.length * 100 / jpeg.length));

      pdfHandler.replaceJpeg(page, newJpegBytes, newJpegWidth, newJpegHeight, newJpegIsGray);

      if (!quiet) {
        logger.info(log.toString());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
